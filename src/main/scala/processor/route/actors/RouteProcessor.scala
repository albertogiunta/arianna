package processor.route.actors

import akka.actor.{ActorRef, ActorSelection}
import com.actors.CustomActor
import com.utils.Practicability
import ontologies.messages.Location._
import ontologies.messages.MessageType.Route
import ontologies.messages._
import processor.route.algorithms.AStarSearch
import processor.route.algorithms.AStarSearch.Graph
import system.names.NamingSystem

import scala.collection.immutable.HashMap
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

/**
  * This Actor transform the Topology in a graph and calculate a given route using Dijkstra Algorithm
  *
  */
class RouteProcessor(val core: ActorRef) extends CustomActor {
    
    val cacheManager: () => ActorSelection = () => sibling(NamingSystem.CacheManager).get
    
    override def receive: Receive = {
        case RouteInfo(req, AreaViewedFromACell(_, rooms)) =>
            req match {
                case RouteRequest(_, actualRoom, _, true) =>
                    val futures = rooms.filter(r => r.info.isExitPoint)
                        .map(exit => RouteProcessor.computeRoute(actualRoom.name, exit.info.id.name, rooms))
                    
                    Future {
                        futures.map(f => Await.result(f, Duration.Inf)).minBy(res => res._2)
                    }.onComplete(p => if (p.isSuccess) {
                        log.info("Found route {} with a cost of {}", p.get._1.mkString(" -> "), p.get._2)
                        core ! ResponseMessage(req.copy(toCell = p.get._1.last), p.get._1)
                    })

                case RouteRequest(_, fromCell, toCell, false) =>
                    RouteProcessor.computeRoute(fromCell.name, toCell.name, rooms)
                        .onComplete(p => if (p.isSuccess) {
    
                            log.info("Found route {} with a cost of {}", p.get._1.mkString(" -> "), p.get._2)
                            
                            log.info("Sending route data for Caching")
    
                            cacheManager() ! RouteResponse(req, p.get._1)
    
                            core ! ResponseMessage(req, p.get._1)
                        })
            }

        case _ => // Ignore
    }
    
    private val ResponseMessage = (req: RouteRequest, route: List[RoomID]) =>
        AriadneMessage(
            Route,
            Route.Subtype.Response,
            Location.Cell >> Location.User,
            RouteResponse(req, route)
        )
    
}

object RouteProcessor {
    
    /**
      * This compute the SHP from Cell A to Cell B,
      * doing the need transformation from the given list of cells to a graph
      *
      * @param fromCell The source cell on which compute the SHP
      * @param toCell   The terget cell on which compute the SHP
      * @param rooms    The List of cell composing the graph
      * @return A Future that will Asynchronously compute the Route
      */
    def computeRoute(fromCell: String, toCell: String, rooms: List[RoomViewedFromACell]): Future[(List[RoomID], Double)] =
        Future[(List[RoomID], Double)] {
            val asMap: Map[String, RoomViewedFromACell] = HashMap(rooms.map(c => c.info.id.name -> c): _*)
    
            val graph: Graph[String] = toGraph(asMap)
            
            val (shp, cost) = AStarSearch.A_*(graph)(fromCell, toCell)(AStarSearch.Extractors.toList)
        
            (shp.map(s => asMap(s).info.id), cost)
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
            map.values.toList.map(cell =>
                cell.info.id.name -> HashMap(
                    cell.neighbors.map(n => {
                        val tmp = Practicability.toWeight(cell.practicability, map(n.id.name).practicability)
                        if (tmp < min) min = tmp
                        n.id.name -> tmp
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