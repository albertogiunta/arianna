package system.cell.processor.route.actors

import akka.actor.{ActorRef, ActorSelection}
import com.actors.CustomActor
import com.utils.Practicability
import system.cell.processor.route.algorithms.AStarSearch
import system.cell.processor.route.algorithms.AStarSearch.Graph
import system.names.NamingSystem
import system.ontologies.messages.Location._
import system.ontologies.messages.MessageType.Route
import system.ontologies.messages._

import scala.collection.immutable.HashMap
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

/**
  * This Actor transform the Topology in a graph and calculate a given route using A* Algorithm
  *
  */
class RouteProcessor(val core: ActorRef) extends CustomActor {
    
    val cacheManager: () => ActorSelection = () => sibling(NamingSystem.CacheManager).get
    
    override def receive: Receive = {
        case RouteInfo(request, AreaViewedFromACell(_, rooms)) =>
            request match {
                case RouteRequest(_, actualRoom, _, isEscape@true) =>
                    val futures = rooms.filter(r => r.info.isExitPoint)
                        .map(exit => RouteProcessor.computeRoute(actualRoom.name, exit.info.id.name, rooms))
                    
                    Future {
                        futures.map(f => Await.result(f, Duration.Inf)).minBy(res => res._2)
                    }.onComplete(p => if (p.isSuccess) {
                        log.info("Found route {} with a cost of {}", p.get._1.mkString(" -> "), p.get._2)
                        core ! Response(request.copy(toCell = p.get._1.last), p.get._1)
                    })
            
                case RouteRequest(_, fromCell, toCell, isEscape@false) =>
                    RouteProcessor.computeRoute(fromCell.name, toCell.name, rooms)
                        .onComplete(p => if (p.isSuccess) {
                            log.info("Found route {} with a cost of {}", p.get._1.mkString(" -> "), p.get._2)
    
                            cacheManager() ! RouteResponse(request, p.get._1)
    
                            core ! Response(request, p.get._1)
                        })
            }

        case _ => // Ignore
    }
    
    private val Response = (request: RouteRequest, route: List[RoomID]) =>
        AriadneMessage(
            Route,
            Route.Subtype.Response,
            Location.Cell >> Location.User,
            RouteResponse(request, route)
        )
    
}

object RouteProcessor {
    
    /**
      * This compute the SHP from Cell A to Cell B,
      * doing the need transformation from the given list of cells to a graph
      *
      * @param fromRoom The source cell on which compute the SHP
      * @param toRoom   The terget cell on which compute the SHP
      * @param rooms    The List of cell composing the graph
      * @return A Future that will Asynchronously compute the Route
      */
    def computeRoute(fromRoom: String, toRoom: String, rooms: List[RoomViewedFromACell]): Future[(List[RoomID], Double)] =
        Future[(List[RoomID], Double)] {
            val indexByName: Map[String, RoomViewedFromACell] = HashMap(rooms.map(r => r.info.id.name -> r): _*)
    
            val graph: Graph[String] = toGraph(indexByName)
    
            val (shp, cost) = AStarSearch.A_*(graph)(fromRoom, toRoom)(AStarSearch.Extractors.toList)
    
            (shp.map(s => indexByName(s).info.id), cost)
        }
    
    /**
      * This method transform the given Map into a normalized graph where all weights are > 0
      *
      * @param map The Map of cells to transform
      * @return The Normalized Graph
      */
    def toGraph(map: Map[String, RoomViewedFromACell]): Graph[String] = {
        var min: Double = 0.0
        
        val graph = HashMap(
            map.values.toList.map(room =>
                room.info.id.name -> HashMap(
                    room.neighbors.map(n => {
                        val tmp = Practicability.toWeight(room.practicability, map(n.name).practicability)
                        if (tmp < min) min = tmp
                        n.name -> tmp
                    }): _*)
            ): _*)
        
        if (min < 0.0)
            HashMap(
                graph.map(n =>
                    n._1 -> HashMap(n._2.mapValues(d => d - min).toSeq: _*)
                ).toSeq: _*)
        else graph
    }
    
}