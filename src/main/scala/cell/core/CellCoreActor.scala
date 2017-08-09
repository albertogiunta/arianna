package cell.core

import akka.actor.{ActorRef, Props}
import cell.cluster.{CellClusterSupervisor, CellPublisher, CellSubscriber}
import cell.sensormanagement.SensorManager
import com.actors.BasicActor
import com.utils.Practicability
import ontologies.messages.AriannaJsonProtocol._
import ontologies.messages.Location._
import ontologies.messages.MessageType.Topology.Subtype.ViewedFromACell
import ontologies.messages.MessageType._
import ontologies.messages._
import processor.route.actors.RouteManager
import spray.json._
import system.names.NamingSystem

import scala.collection.mutable
import scala.io.Source
import scala.util.Random

/**
  * This is the main actor of a cell, it provide the main cell management and
  * the other cell's actors initialization
  * Created by Matteo Gabellini on 14/07/2017.
  */
class CellCoreActor(mediator: ActorRef) extends BasicActor {

    private var actualSelfLoad: Int = 0
    private var localCellInfo: CellInfo = CellInfo.empty
    private var sensorsMounted: List[SensorInfo] = List.empty[SensorInfo]
    
    private val topology: mutable.Map[String, RoomViewedFromACell] = mutable.HashMap.empty
    private val indexByUri: mutable.Map[String, String] = mutable.HashMap.empty

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
    
        cellSubscriber = context.actorOf(Props(new CellSubscriber(mediator)), NamingSystem.Subscriber)
        cellPublisher = context.actorOf(Props(new CellPublisher(mediator)), NamingSystem.Publisher)

        sensorManager = context.actorOf(Props[SensorManager], NamingSystem.SensorManager)
        userActor = context.actorOf(Props[UserManager], NamingSystem.UserManager)
        routeManager = context.actorOf(Props[RouteManager], NamingSystem.RouteManager)


    }

    override protected def init(args: List[Any]): Unit = {
        log.info("Hello there! the cell core is being initialized")

        clusterListener = context.actorOf(Props[CellClusterSupervisor], NamingSystem.CellClusterSupervisor)
        val cellConfiguration = Source.fromFile(args.head.asInstanceOf[String]).getLines.mkString
        val loadedConfig = cellConfiguration.parseJson.convertTo[CellConfig]
        localCellInfo = loadedConfig.cellInfo

        sensorManager ! AriadneMessage(Init,
            Init.Subtype.Greetings,
            self2Self,
            Greetings(List(loadedConfig.sensors.toJson.toString())))
    }

    override protected def receptive: Receive = {

        case msg@AriadneMessage(Info, Info.Subtype.Request, this.self2Self, cnt: SensorsInfoUpdate) => {
            println("aaa 1")
            //Informations request from the cell publisher in order to complete the handshake task with the master
            if (localCellInfo == CellInfo.empty || sensorsMounted.isEmpty) {
                stash()
            } else {
                sender() ! msg.copy(
                    subtype = Info.Subtype.Response,
                    content = SensorsInfoUpdate(localCellInfo, sensorsMounted)
                )
                println("aaa 2")
            }
        }

        case msg@AriadneMessage(Update, Update.Subtype.Sensors, this.self2Self, cnt: SensorsInfoUpdate) => {
            if (sensorsMounted.isEmpty) {
                sensorsMounted = cnt.sensors
                unstashAll()
            } else {
                sensorsMounted = cnt.sensors
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
                Greetings(List(localCellInfo.uri, localCellInfo.port.toString)))
            userActor ! msg.copy(direction = cell2User)
        }

        case _ => desist _
    }

    protected def cultured: Receive = {
        case msg@AriadneMessage(Update, Update.Subtype.Sensors, this.self2Self, cnt: SensorsInfoUpdate) => {
            cellPublisher ! msg.copy(content = cnt.copy(cell = this.localCellInfo))
        }

        case AriadneMessage(Update, Update.Subtype.Practicability, this.cell2Cell, cnt: PracticabilityUpdate) =>
            topology.put(cnt.room.name, topology(cnt.room.name)
                .copy(practicability = cnt.practicability))

        case msg@AriadneMessage(Update, Update.Subtype.CurrentPeople, this.user2Cell, cnt: CurrentPeopleUpdate) => {

            actualSelfLoad = cnt.currentPeople

            topology.put(cnt.room.name, topology(localCellInfo.uri).copy(practicability =
                Practicability(
                    topology(localCellInfo.uri).info.capacity,
                    cnt.currentPeople,
                    topology(localCellInfo.uri).passages.length)))

            cellPublisher ! msg.copy(direction = cell2Server)
            cellPublisher ! AriadneMessage(
                Update,
                Update.Subtype.Practicability,
                cell2Cell,
                PracticabilityUpdate(
                    topology(indexByUri(localCellInfo.uri)).info.id,
                    topology(localCellInfo.uri).practicability
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

        case msg@AriadneMessage(Alarm, _, this.self2Self, _) => {
            //Alarm triggered in the current cell
            //Check if the topology is initialized
            if (topology.nonEmpty) {
                val currentCell: RoomViewedFromACell = topology(localCellInfo.uri)
                val msgToSend = msg.copy(direction = cell2Cluster,
                    content = AlarmContent(localCellInfo, currentCell.info)
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
                    RouteRequest(id, topology(localCellInfo.uri).info.id, RoomID.empty, isEscape = true),
                    AreaViewedFromACell(Random.nextInt(), area)
                )
            )
        }

        case _ => desist _
    }
}
