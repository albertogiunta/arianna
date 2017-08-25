package system.cell.core

import java.net.InetAddress

import akka.actor.{ActorRef, Props}
import com.actors.TemplateActor
import com.utils.Practicability
import spray.json._
import system.cell.cluster.{CellClusterSupervisor, CellPublisher, CellSubscriber}
import system.cell.processor.route.actors.RouteManager
import system.cell.sensormanagement.SensorManager
import system.cell.userManagement.UserManager
import system.exceptions.IncorrectConfigurationException
import system.names.NamingSystem
import system.ontologies.messages.AriannaJsonProtocol._
import system.ontologies.messages.MessageType.Topology.Subtype.ViewedFromACell
import system.ontologies.messages.MessageType._
import system.ontologies.messages._

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.io.Source
import scala.util.Random

/**
  * This is the main actor of a cell, it provide the main cell management and
  * the other cell's actors initialization
  * Created by Matteo Gabellini on 14/07/2017.
  */
class CellCoreActor(mediator: ActorRef) extends TemplateActor {

    private var actualSelfLoad: Int = 0
    private var localCellInfo: CellInfo = CellInfo.empty
    private var sensorsMounted: List[SensorInfo] = List.empty[SensorInfo]
    
    private val topology: mutable.Map[String, RoomViewedFromACell] = mutable.HashMap.empty
    private val indexByUri: mutable.Map[String, String] = mutable.HashMap.empty

    private val practicabilityToBeRestored: mutable.Map[String, Double] = mutable.HashMap.empty

    var clusterListener: ActorRef = _
    var cellPublisher: ActorRef = _
    var cellSubscriber: ActorRef = _
    var sensorManager: ActorRef = _
    var userActor: ActorRef = _
    var routeManager: ActorRef = _


    override def preStart: Unit = {
        super.preStart()

        cellSubscriber = context.actorOf(Props(new CellSubscriber(mediator)), NamingSystem.Subscriber)
        cellPublisher = context.actorOf(Props(new CellPublisher(mediator)), NamingSystem.Publisher)

        sensorManager = context.actorOf(Props[SensorManager], NamingSystem.SensorManager)
        userActor = context.actorOf(Props[UserManager], NamingSystem.UserManager)
        routeManager = context.actorOf(Props[RouteManager], NamingSystem.RouteManager)


    }
    
    override protected def init(args: List[String]): Unit = {
        log.debug("Hello there! the cell core is being initialized")

        clusterListener = context.actorOf(Props[CellClusterSupervisor], NamingSystem.CellClusterSupervisor)
        val cellConfiguration = Source.fromFile(args.head.asInstanceOf[String]).getLines.mkString
        val loadedConfig = cellConfiguration.parseJson.convertTo[CellConfig]
        if (loadedConfig.cellInfo == CellInfo.empty) throw IncorrectConfigurationException(this.name)
    
        localCellInfo = loadedConfig.cellInfo.copy(ip = InetAddress.getLocalHost.getHostAddress)
        sensorManager ! AriadneMessage(Init,
            Init.Subtype.Greetings,
            Location.PreMade.selfToSelf,
            Greetings(List(loadedConfig.sensors.toJson.toString())))
    }

    override protected def receptive: Receive = {

        case msg@AriadneMessage(Info, Info.Subtype.Request, Location.PreMade.selfToSelf, cnt: SensorsInfoUpdate) => {
            //Informations request from the cell publisher in order to complete the handshake task with the system.master
            if (sensorsMounted.isEmpty) {
                log.debug("Sensor Data not yet ready, stash the info request")
                stash()
            } else {
                sender() ! msg.copy(
                    subtype = Info.Subtype.Response,
                    content = SensorsInfoUpdate(localCellInfo, sensorsMounted)
                )
            }
        }

        case msg@AriadneMessage(Update, Update.Subtype.Sensors, Location.PreMade.selfToSelf, cnt: SensorsInfoUpdate) => {
            if (sensorsMounted.isEmpty) {
                sensorsMounted = cnt.sensors
                unstashAll()
            } else {
                sensorsMounted = cnt.sensors
            }
        }

        case msg@AriadneMessage(Error, Error.Subtype.CellMappingMismatch, _, cnt: Empty) =>
            log.error("Mapping Error")

        case msg@AriadneMessage(Topology, ViewedFromACell, Location.PreMade.masterToCell, cnt: AreaViewedFromACell) => {
            log.info(s"Topology arrived from Master $cnt")
            log.info("Sending ACK to Master for Topology...")
    
            cellPublisher ! AriadneMessage(
                Topology,
                Topology.Subtype.Acknowledgement,
                Location.PreMade.selfToSelf,
                localCellInfo
            )
    
            cnt.rooms.foreach(room => topology += room.info.id.name -> room)
            cnt.rooms.foreach(room => indexByUri += room.cell.uri -> room.info.id.name)

            userActor ! AriadneMessage(
                Init,
                Init.Subtype.Greetings,
                Location.PreMade.selfToSelf,
                Greetings(List(localCellInfo.uri, localCellInfo.port.toString)))

            userActor ! msg.copy(direction = Location.PreMade.cellToUser)

            this.context.become(cultured, discardOld = true)
            log.info("I've become cultured")
        }

        case _ => desist _
    }

    protected def cultured: Receive = ({

        case msg@AriadneMessage(Alarm, _, Location.PreMade.selfToSelf, _) => {
            //Alarm triggered in the current cell
            val currentCell: RoomViewedFromACell = topology(indexByUri(localCellInfo.uri))
            val msgToSend = msg.copy(
                content = AlarmContent(localCellInfo, currentCell.info)
            )
            cellPublisher ! msgToSend
            context.become(localEmergency, discardOld = true)
            log.info("Alarm triggered locally")
        }

        case msg@AriadneMessage(Alarm, Alarm.Subtype.End, _, _) =>
            userActor ! msg
            this.updatePracticabilityOnAlarmEnd()
            log.info("Alarm deactiveted")

        case msg@AriadneMessage(Update, Update.Subtype.CurrentPeople, Location.PreMade.userToCell, cnt: CurrentPeopleUpdate) => {

            actualSelfLoad = cnt.currentPeople
    
            topology += indexByUri(localCellInfo.uri) -> topology(indexByUri(localCellInfo.uri)).copy(practicability = updatedPracticability())
            
            cellPublisher ! msg.copy(content = cnt.copy(room = topology(indexByUri(cnt.room.name)).info.id))
            cellPublisher ! AriadneMessage(
                Update,
                Update.Subtype.Practicability,
                Location.PreMade.selfToSelf,
                PracticabilityUpdate(
                    topology(indexByUri(localCellInfo.uri)).info.id,
                    topology(indexByUri(localCellInfo.uri)).practicability
                )
            )
        }
    }: Receive) orElse this.proactive
    
    
    protected def localEmergency: Receive = ({
        case msg@AriadneMessage(Alarm, Alarm.Subtype.End, _, _) => {
            userActor ! msg
            context.become(cultured, discardOld = true)
    
            practicabilityToBeRestored += indexByUri(localCellInfo.uri) -> updatedPracticability()
            
            this.updatePracticabilityOnAlarmEnd()
    
            cellPublisher ! AriadneMessage(
                Update,
                Update.Subtype.Practicability,
                Location.PreMade.selfToSelf,
                PracticabilityUpdate(
                    topology(indexByUri(localCellInfo.uri)).info.id,
                    topology(indexByUri(localCellInfo.uri)).practicability
                )
            )
            log.info("Alarm deactiveted")
        }

        case msg@AriadneMessage(Update, Update.Subtype.CurrentPeople, Location.PreMade.userToCell, cnt: CurrentPeopleUpdate) => {
            actualSelfLoad = cnt.currentPeople
            cellPublisher ! msg.copy(content = cnt.copy(room = topology(indexByUri(cnt.room.name)).info.id))
        }

    }: Receive) orElse this.proactive
    
    
    private def proactive: Receive = {
        case msg@AriadneMessage(Init, Init.Subtype.Goodbyes, _, _) => {
            log.info("Ariadne system is shutting down...")
            context.system.terminate().onComplete(_ => println("Ariadne system has shutdown!"))
            System.exit(0)
        }

        case msg@AriadneMessage(Update, Update.Subtype.Sensors, Location.PreMade.selfToSelf, cnt: SensorsInfoUpdate) => {
            cellPublisher ! msg.copy(content = cnt.copy(cell = this.localCellInfo))
        }

        case AriadneMessage(Update, Update.Subtype.Practicability, Location.PreMade.cellToCell, cnt: PracticabilityUpdate) => {
            if (topology(cnt.room.name).practicability == Double.PositiveInfinity) {
                /*
                * save the practicability update of a cell considered in alarm to prevent
                * the receiving ordering problem between Alarm, Alarm End and Practicability Update messages
                * sent from a cell in alarm during the alarm deactivation
                * */
                practicabilityToBeRestored += cnt.room.name -> cnt.practicability
            } else {
                topology += cnt.room.name -> topology(cnt.room.name).copy(practicability = cnt.practicability)
            }
        }

        case AriadneMessage(Route, Route.Subtype.Request, Location.PreMade.userToCell, cnt: RouteRequest) => {
            //route request from user management
            routeManager ! AriadneMessage(
                Route,
                Route.Subtype.Info,
                Location.PreMade.selfToSelf,
                RouteInfo(
                    cnt,
                    AreaViewedFromACell(Random.nextInt(), topology.values.toList)
                )
            )
        }

        case msg@AriadneMessage(Route, Route.Subtype.Response, Location.PreMade.cellToUser, _) =>
            //route response from route manager for the user
            userActor ! msg

        case AriadneMessage(Alarm, _, _, alarm) => {
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
                Location.PreMade.selfToSelf,
                RouteInfo(
                    RouteRequest(id, topology(indexByUri(localCellInfo.uri)).info.id, RoomID.empty, isEscape = true),
                    AreaViewedFromACell(Random.nextInt(), area)
                )
            )
        }

        case msg@AriadneMessage(Topology, ViewedFromACell, Location.PreMade.masterToCell, cnt: AreaViewedFromACell) => {
            //The master did not receive the ack -> resend acknowledgement
            cellPublisher ! AriadneMessage(
                Topology,
                Topology.Subtype.Acknowledgement,
                Location.PreMade.selfToSelf,
                localCellInfo
            )
        }

        case _ => desist _
    }

    private def updatePracticabilityOnAlarmEnd(): Unit = {
        practicabilityToBeRestored.keys.foreach(X =>
            topology += X -> topology(X).copy(practicability = practicabilityToBeRestored(X))
        )
        practicabilityToBeRestored.clear()
    }

    private def updatedPracticability(): Double = {
        Practicability(
            topology(indexByUri(localCellInfo.uri)).info.capacity,
            actualSelfLoad,
            topology(indexByUri(localCellInfo.uri)).passages.length
        )
    }
}
