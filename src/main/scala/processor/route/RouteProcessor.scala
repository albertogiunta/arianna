package processor.route

import akka.actor.{ActorRef, ActorSelection}
import common.BasicActor
import ontologies.messages.Location._
import ontologies.messages.MessageType.Route
import ontologies.messages.MessageType.Route.Subtype.{Escape, Info, Response}
import ontologies.messages._
import processor.route.algorithms.AStarSearch
import processor.route.algorithms.Dijkstra.Graph

import scala.collection.immutable.HashMap
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

/**
  * This Actor transform the Topology in a graph and calculate a given route using Dijkstra Algorithm
  *
  */
private class RouteProcessor(val router: ActorRef) extends BasicActor {
    
    var cacher: ActorSelection = _
    
    override protected def init(args: List[Any]): Unit = {
        log.info("Hello there from {}", name)
        cacher = sibling("CacheManager").get
    }
    
    override protected def receptive: Receive = {
        case AriadneMessage(Route, Info, _, RouteInfo(req, AreaForCell(_, cells))) =>
            
            computeRoute(req.fromCell.name, req.toCell.name, cells)
                .onComplete(p => if (p.isSuccess) {
                    val msg = AriadneMessage(
                        Route,
                        Response,
                        Location.Cell >> Location.User,
                        RouteResponse(req, p.get._1)
                    )
                    cacher ! msg
                    router ! msg
                })
        
        case AriadneMessage(Route, Escape.Request, _, EscapeRequest(cell, AreaForCell(_, cells))) =>
            
            val futures = cells.filter(c => c.isExitPoint)
                .map(c => computeRoute(cell.name, c.infoCell.name, cells))
            
            Future {
                futures.map(f => Await.result(f, Duration.Inf)).minBy(res => res._2)._1
            }.onComplete(p => if (p.isSuccess) {
                val msg = AriadneMessage(
                    Route,
                    Escape.Response,
                    Location.Cell >> Location.User,
                    EscapeResponse(cell, p.get)
                )
                router ! msg
            })
        case _ => desist _
    }
    
    def computeRoute(fromCell: String, toCell: String, cells: List[CellForCell]): Future[(List[InfoCell], Double)] =
        Future[(List[InfoCell], Double)] {
            val asMap: Map[String, CellForCell] = HashMap(cells.map(c => c.infoCell.name -> c): _*)
            
            val graph: Graph[String] =
                HashMap(
                    cells.map(cell =>
                        cell.infoCell.name ->
                            HashMap(cell.neighbors
                                // remove the source cell from it's respective neighbours
                                .filter(c => c.name != fromCell).map(c =>
                                c.name -> (100.0 + asMap(c.name).practicabilityLevel - cell.practicabilityLevel)
                            ): _*)
                    ): _*)
            
            val (shp, cost) = AStarSearch.A_*(graph)(fromCell, toCell)(AStarSearch.Extractors.toList)
            
            (shp.map(s => asMap(s).infoCell), cost)
        }
}
