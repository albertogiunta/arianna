package processor.route

import akka.actor.{ActorRef, ActorSelection}
import common.CustomActor
import ontologies.messages.Location._
import ontologies.messages.MessageType.Route
import ontologies.messages.MessageType.Route.Subtype.Escape
import ontologies.messages._
import processor.route.algorithms.AStarSearch
import processor.route.algorithms.AStarSearch.Graph

import scala.collection.immutable.HashMap
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

/**
  * This Actor transform the Topology in a graph and calculate a given route using Dijkstra Algorithm
  *
  */
private class RouteProcessor(val router: ActorRef) extends CustomActor {
    
    var cacher: ActorSelection = _
    
    
    override def preStart() = {
        super.preStart()
        
        cacher = sibling("CacheManager").get
    }
    
    override def receive: Receive = {
        case RouteInfo(req, AreaForCell(_, cells)) =>
            
            computeRoute(req.fromCell.name, req.toCell.name, cells)
                .onComplete(p => if (p.isSuccess) {
    
                    val cnt = RouteResponse(req, p.get._1)
    
                    log.info("Sending route data for Caching")
    
                    cacher ! cnt
    
                    router ! AriadneMessage(
                        Route,
                        Route.Subtype.Response,
                        Location.Cell >> Location.User,
                        cnt
                    )
                })
        
        case EscapeRequest(actualCell, AreaForCell(_, cells)) =>
            
            val futures = cells.filter(c => c.isExitPoint)
                .map(c => computeRoute(actualCell.name, c.infoCell.name, cells))
    
            Future {
                futures.map(f => Await.result(f, Duration.Inf)).minBy(res => res._2)._1
            }.onComplete(p => if (p.isSuccess) {
                val msg = AriadneMessage(
                    Route,
                    Escape.Response,
                    Location.Cell >> Location.User,
                    EscapeResponse(actualCell, p.get)
                )
                router ! msg
            })
        case _ =>
    }
    
    def computeRoute(fromCell: String, toCell: String, cells: List[CellForCell]): Future[(List[InfoCell], Double)] =
        Future[(List[InfoCell], Double)] {
            val asMap: Map[String, CellForCell] = HashMap(cells.map(c => c.infoCell.name -> c): _*)
            
            val graph: Graph[String] =
                HashMap(
                    cells.map(cell =>
                        cell.infoCell.name ->
                            HashMap(cell.neighbors.map(neighbor =>
                                neighbor.name -> Math.max(
                                    0.0, 100.0 + asMap(neighbor.name).practicabilityLevel - cell.practicabilityLevel
                                )
                            ): _*)
                    ): _*)
            
            val (shp, cost) = AStarSearch.A_*(graph)(fromCell, toCell)(AStarSearch.Extractors.toList)
    
            log.info("Found route {} with a cost of {}", shp.mkString(" -> "), cost)
            
            (shp.map(s => asMap(s).infoCell), cost)
        }
}
