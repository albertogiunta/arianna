package cell.core

import akka.actor.{ActorRef, Props}
import cell.cluster.{CellPublisher, CellSubscriber}
import common.{BasicActor, ClusterMembersListener}
import ontologies.messages.Location._
import ontologies.messages.MessageType.Topology.Subtype.Topology4Cell
import ontologies.messages.MessageType._
import ontologies.messages._

import scala.collection.mutable

/**
  * Created by Matteo Gabellini on 14/07/2017.
  */
class CellCoreActor extends BasicActor {

    private var topology: mutable.Map[String, CellForCell] = mutable.Map[String, CellForCell]()

    private var actualCellLoad: ActualLoadUpdate = null

    var clusterListener: ActorRef = null
    var cellPublisher: ActorRef = null
    var cellSubscriber: ActorRef = null
    var userActor: ActorRef = null

    private val server2Cell: MessageDirection = Location.Server >> Location.Cell
    private val cell2Server: MessageDirection = Location.Server << Location.Cell
    private val cell2User: MessageDirection = Location.Cell >> Location.User
    private val user2Cell: MessageDirection = Location.Cell << Location.User

    override def preStart(): Unit = {
        super.preStart()
        clusterListener = context.actorOf(Props[ClusterMembersListener], "CellClusterListener")
        cellPublisher = context.actorOf(Props[CellPublisher], "CellPublisher")
        cellSubscriber = context.actorOf(Props[CellSubscriber], "CellSubsciber")
        //userActor = context.actorOf(Props[UserActor], "UserActor")
    }

    override protected def init(args: List[Any]): Unit = {
        log.info("Hello there! the cell core has been initialized")
    }

    override protected def receptive: Receive = {

        case msg@AriadneMessage(Topology, Topology4Cell, server2Cell, cnt: AreaForCell) =>
            cnt.cells.foreach(X => topology.put(X.infoCell.name, X))
            userActor ! msg.copy(direction = cell2User)
        case msg@AriadneMessage(Update, Update.Subtype.Practicability, server2Cell, cnt: LightCell) =>
            topology.put(cnt.info.name, topology(cnt.info.name).copy(practicabilityLevel = cnt.practicabilityLevel))
        case msg@AriadneMessage(Update, Update.Subtype.ActualLoad, user2Cell, cnt: ActualLoadUpdate) =>
            actualCellLoad = cnt
            cellPublisher ! msg.copy(direction = cell2Server)

        case msg@AriadneMessage(Route, Route.Subtype.Basic, user2Cell, cnt) =>
        //route request from user management
        //routeManager ! msg with topology
        case msg@AriadneMessage(Route, Route.Subtype.Basic, cell2User, cnt) =>
            //route response from route manager for the user
            userActor ! msg
        case msg@AriadneMessage(Alarm, Alarm.Subtype.Basic, server2Cell, cnt) =>
        //request to the route manager the escape route
        //routeManager ! msg with topology
        case msg@AriadneMessage(Route, Route.Subtype.Escape, cell2User, cnt) =>
            //route response from route manager for the user with the Escape route
            userActor ! msg

    }
}
