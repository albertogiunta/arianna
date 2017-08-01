package cell.core

import akka.actor.{ActorRef, Props}
import cell.cluster.{CellPublisher, CellSubscriber}
import cell.processor.route.actors.RouteManager
import com.actors.{BasicActor, ClusterMembersListener}
import ontologies.messages.Location._
import ontologies.messages.MessageType.Topology.Subtype.ViewedFromACell
import ontologies.messages.MessageType._
import ontologies.messages._

import scala.collection.mutable
import scala.util.Random

/**
  * Created by Matteo Gabellini on 14/07/2017.
  */
class CellCoreActor extends BasicActor {

    private val greetings: String = "Hello there, it's time to dress-up"
    
    private val uri: String = "uri1"
    private val topology: mutable.Map[String, CellViewedFromACell] = mutable.HashMap[String, CellViewedFromACell]()
    
    private var actualSelfLoad: Int = 0

    var clusterListener: ActorRef = _
    var cellPublisher: ActorRef = _
    var cellSubscriber: ActorRef = _
    var userActor: ActorRef = _
    var routeManager: ActorRef = _
    
    private val server2Cell: MessageDirection = Location.Master >> Location.Cell
    private val cell2Server: MessageDirection = Location.Master << Location.Cell
    private val cell2Cell: MessageDirection = Location.Cell << Location.Cell
    private val cell2User: MessageDirection = Location.Cell >> Location.User
    private val user2Cell: MessageDirection = Location.Cell << Location.User

    override def preStart(): Unit = {
        super.preStart()
        clusterListener = context.actorOf(Props[ClusterMembersListener], "CellClusterListener")
    
        cellPublisher = context.actorOf(Props[CellPublisher], "CellPublisher")
        cellSubscriber = context.actorOf(Props[CellSubscriber], "CellSubscriber")
    
        userActor = context.actorOf(Props[UserManager], "UserManager")
        routeManager = context.actorOf(Props[RouteManager], "RouteManager")
    }

    override protected def init(args: List[Any]): Unit = {
        log.info("Hello there! the cell core has been initialized")
        userActor ! AriadneMessage(Init, Init.Subtype.Greetings,
            Location.Master >> Location.Self, Greetings(List(greetings)))
    }

    override protected def receptive: Receive = {
    
        case msg@AriadneMessage(Topology, ViewedFromACell, `server2Cell`, cnt: AreaViewedFromACell) =>
            println(s"Area arrived from Server $cnt")
            cnt.cells.foreach(X => topology.put(X.info.uri, X))
            userActor ! msg.copy(direction = cell2User)

        case AriadneMessage(Update, Update.Subtype.Practicability, `cell2Cell`, cnt: PracticabilityUpdate) =>
            topology.put(cnt.info.uri, topology(cnt.info.uri)
                .copy(practicability = cnt.practicability))

        case msg@AriadneMessage(Update, Update.Subtype.CurrentPeople, `user2Cell`, cnt: CurrentPeopleUpdate) =>
    
            actualSelfLoad = cnt.currentPeople
    
            topology.put(uri, topology(uri).copy(practicability =
                weight(topology(uri).capacity, cnt.currentPeople, topology(uri).passages.length)))
            
            cellPublisher ! msg.copy(direction = cell2Server)
            cellPublisher ! AriadneMessage(
                Update,
                Update.Subtype.Practicability,
                cell2Cell,
                PracticabilityUpdate(
                    topology(uri).info,
                    topology(uri).practicability
                )
            )
    
        case AriadneMessage(Route, Route.Subtype.Request, `user2Cell`, cnt: RouteRequest) =>
            //route request from user management
            routeManager ! AriadneMessage(
                Route,
                Route.Subtype.Info,
                Location.Self >> Location.Self,
                RouteInfo(
                    cnt,
                    AreaViewedFromACell(Random.nextInt(), topology.values.toList)
                )
            )
    
        case msg@AriadneMessage(Route, Route.Subtype.Response, `cell2User`, _) =>
            //route response from route manager for the user
            userActor ! msg

        case AriadneMessage(Alarm, _, _, alarm) =>
            val (id, area) = alarm match {
                case AlarmContent(compromisedCell, _, _) =>
                    ("-1", topology.values.map(cell => {
                        if (cell.info.uri == compromisedCell.uri)
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
                Location.Self >> Location.Self,
                RouteInfo(
                    RouteRequest(id, topology(uri).info, InfoCell.empty, isEscape = true),
                    AreaViewedFromACell(Random.nextInt(), area)
                )
            )
    }

    private def weight(capacity: Int, load: Int, flows: Int): Double = {
        val log_b: (Double, Double) => Double = (b, n) => Math.log(n) / Math.log(b)
        1 / (load * 1.05 / capacity * (
            if (flows == 1) 0.25 else if (flows > 4.0) log_b(3.0, 4.25) else log_b(3.0, flows))
            )
    }
}
