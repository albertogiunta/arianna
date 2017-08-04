package cell.core

import akka.actor.{ActorRef, Props}
import cell.cluster.{CellPublisher, CellSubscriber}
import cell.sensormanagement.SensorManager
import com.actors.{BasicActor, ClusterMembersListener}
import com.utils.Practicability
import ontologies.messages.AriannaJsonProtocol._
import ontologies.messages.Location._
import ontologies.messages.MessageType.Topology.Subtype.ViewedFromACell
import ontologies.messages.MessageType._
import ontologies.messages._
import processor.route.actors.RouteManager
import spray.json._

import scala.collection.mutable
import scala.io.Source
import scala.util.Random

/**
  * This is the main actor of a cell, it provide the main cell management and
  * the other cell's actors initialization
  * Created by Matteo Gabellini on 14/07/2017.
  */
class CellCoreActor extends BasicActor {
    
    private var infoCell: CellInfo = CellInfo.empty
    private var sensorsMounted: List[SensorInfo] = List.empty[SensorInfo]
    
    private val topology: mutable.Map[String, RoomViewedFromACell] = mutable.HashMap.empty
    private val indexByUri: mutable.Map[String, String] = mutable.HashMap.empty
    
    private var actualSelfLoad: Int = 0

    var clusterListener: ActorRef = _
    var cellPublisher: ActorRef = _
    var cellSubscriber: ActorRef = _
    var sensorManager: ActorRef = _
    var userActor: ActorRef = _
    var routeManager: ActorRef = _

    private val self2Self: MessageDirection = Location.Self >> Location.Self
    private val server2Cell: MessageDirection = Location.Master >> Location.Cell
    private val cell2Server: MessageDirection = Location.Master << Location.Cell
    private val cell2Cell: MessageDirection = Location.Cell << Location.Cell
    private val cell2Cluster: MessageDirection = Location.Cell >> Location.Cluster
    private val cell2User: MessageDirection = Location.Cell >> Location.User
    private val user2Cell: MessageDirection = Location.Cell << Location.User

    override def preStart: Unit = {
        super.preStart()
        clusterListener = context.actorOf(Props[ClusterMembersListener], "CellClusterListener")

        cellSubscriber = context.actorOf(Props[CellSubscriber], "CellSubscriber")
        cellPublisher = context.actorOf(Props[CellPublisher], "CellPublisher")

        sensorManager = context.actorOf(Props[SensorManager], "SensorManager")
        userActor = context.actorOf(Props[UserManager], "UserManager")
        routeManager = context.actorOf(Props[RouteManager], "RouteManager")
    }

    override protected def init(args: List[Any]): Unit = {
        log.info("Hello there! the cell core has been initialized")

        val cellConfiguration = Source.fromFile(s"res/json/cell/cell2.json").getLines.mkString
        val loadedInfo = cellConfiguration.parseJson.convertTo[CellConfig]
        infoCell = infoCell.copy(uri = loadedInfo.uri)

        sensorManager ! AriadneMessage(Init,
            Init.Subtype.Greetings,
            self2Self,
            Greetings(List(loadedInfo.sensors.toJson.toString())))
    }

    override protected def receptive: Receive = {

        case msg@AriadneMessage(Info, Info.Subtype.Request, this.self2Self, cnt: SensorsInfoUpdate) => {
            //Informations request from the cell publisher in order to complete the handshake task with the master
            if (infoCell == CellInfo.empty || sensorsMounted.isEmpty) {
                stash()
            } else {
                sender() ! msg.copy(
                    subtype = Info.Subtype.Response,
                    content = SensorsInfoUpdate(infoCell, sensorsMounted)
                )
            }
        }

        case msg@AriadneMessage(Error, Error.Subtype.CellMappingMismatch, _, cnt: Empty) =>
            log.error("Mapping Error")

        case msg@AriadneMessage(Topology, ViewedFromACell, this.server2Cell, cnt: AreaViewedFromACell) => {
            log.info(s"Area arrived from Server $cnt")
            
            cnt.rooms.foreach(room => topology.put(room.info.id.name, room))
            cnt.rooms.foreach(room => indexByUri.put(room.cell.uri, room.info.id.name))
            
            userActor ! AriadneMessage(Init,
                Init.Subtype.Greetings,
                self2Self,
                Greetings(List(infoCell.uri, infoCell.port.toString)))
            Thread.sleep(3000)
            userActor ! msg.copy(direction = cell2User)
        }

        case msg@AriadneMessage(Update, Update.Subtype.Sensors, this.self2Self, cnt: SensorsInfoUpdate) => {
            if (sensorsMounted.isEmpty) {
                sensorsMounted = cnt.sensors
                unstashAll()
            } else {
                sensorsMounted = cnt.sensors
                cellPublisher ! msg.copy(content = cnt.copy(cell = this.infoCell))
            }
        }

        case AriadneMessage(Update, Update.Subtype.Practicability, this.cell2Cell, cnt: PracticabilityUpdate) =>
            topology.put(cnt.room.name, topology(cnt.room.name)
                .copy(practicability = cnt.practicability))

        case msg@AriadneMessage(Update, Update.Subtype.CurrentPeople, this.user2Cell, cnt: CurrentPeopleUpdate) => {

            actualSelfLoad = cnt.currentPeople
            
            topology.put(cnt.room.name, topology(infoCell.uri).copy(practicability =
                Practicability(
                    topology(infoCell.uri).info.capacity,
                    cnt.currentPeople,
                    topology(infoCell.uri).passages.length)))

            cellPublisher ! msg.copy(direction = cell2Server)
            cellPublisher ! AriadneMessage(
                Update,
                Update.Subtype.Practicability,
                cell2Cell,
                PracticabilityUpdate(
                    topology(infoCell.uri).cell,
                    topology(infoCell.uri).practicability
                )
            )
        }

        case AriadneMessage(Route, Route.Subtype.Request, this.user2Cell, cnt: RouteRequest) => {
            //route request from user management
            routeManager ! AriadneMessage(
                Route,
                Route.Subtype.Info,
                self2Self,
                RouteInfo(
                    cnt,
                    AreaViewedFromACell(Random.nextInt(), topology.values.toList)
                )
            )
        }

        case msg@AriadneMessage(Route, Route.Subtype.Response, this.cell2User, _) =>
            //route response from route manager for the user
            userActor ! msg

        case msg@AriadneMessage(Alarm, _, this.self2Self, cnt) => {
            //Alarm triggered in the current cell
            //Check if the topology is initialized
            if (topology.size != 0) {
                val currentCell: RoomViewedFromACell = topology.get(infoCell.uri).get
                val msgToSend = msg.copy(direction = cell2Cluster,
                    content = AlarmContent(infoCell, currentCell.info)
                )
                cellPublisher ! msgToSend
            }
        }

        case AriadneMessage(Alarm, _, this.cell2Cluster, alarm) => {
            val (id, area) = alarm match {
                case AlarmContent(compromisedCell, _) =>
                    ("-1", topology.values.map(cell => {
                        if (cell.cell.uri == compromisedCell.uri)
                            cell.copy(practicability = Double.PositiveInfinity)
                        else cell
                    }).toList)
                case _ =>
                    ("0", topology.values.toList)
            }
            //request to the route manager the escape route
            routeManager ! AriadneMessage(
                Route,
                Route.Subtype.Info,
                self2Self,
                RouteInfo(
                    RouteRequest(id, topology(infoCell.uri).info.id, RoomID.empty, isEscape = true),
                    AreaViewedFromACell(Random.nextInt(), area)
                )
            )
        }
    }
}
