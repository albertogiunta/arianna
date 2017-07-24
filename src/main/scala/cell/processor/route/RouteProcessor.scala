package cell.processor.route

import akka.actor.{ActorRef, ActorSelection}
import cell.processor.route.algorithms.AStarSearch
import cell.processor.route.algorithms.AStarSearch.Graph
import common.CustomActor
import ontologies.messages.Location._
import ontologies.messages.MessageType.Route
import ontologies.messages._

import scala.collection.immutable.HashMap
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

/**
  * This Actor transform the Topology in a graph and calculate a given route using Dijkstra Algorithm
  *
  */
private class RouteProcessor(val core: ActorRef) extends CustomActor {

    val cacheManager: () => ActorSelection = () => sibling("CacheManager").get

    override def receive: Receive = {
        case RouteInfo(req, AreaViewedFromACell(_, cells)) =>
            req match {
                case RouteRequest(_, actualCell, _, true) =>
                    val futures = cells.filter(c => c.isExitPoint)
                        .map(exit => computeRoute(actualCell.name, exit.info.name, cells))

                    Future {
                        futures.map(f => Await.result(f, Duration.Inf)).minBy(res => res._2)._1
                    }.onComplete(p => if (p.isSuccess) {
                        core ! ResponseMessage(req.copy(toCell = p.get.last), p.get)
                    })

                case RouteRequest(_, fromCell, toCell, false) =>
                    computeRoute(fromCell.name, toCell.name, cells)
                        .onComplete(p => if (p.isSuccess) {

                            log.info("Sending route data for Caching")

                            cacheManager() ! RouteResponse(req, p.get._1)

                            core ! ResponseMessage(req, p.get._1)
                        })
            }

        case _ => // Ignore
    }

    private val ResponseMessage = (req: RouteRequest, route: List[InfoCell]) =>
        AriadneMessage(
            Route,
            Route.Subtype.Response,
            Location.Cell >> Location.User,
            RouteResponse(req, route)
        )


    def computeRoute(fromCell: String, toCell: String, cells: List[CellViewedFromACell]): Future[(List[InfoCell], Double)] =
        Future[(List[InfoCell], Double)] {
            val asMap: Map[String, CellViewedFromACell] = HashMap(cells.map(c => c.info.name -> c): _*)

            val graph: Graph[String] =
                HashMap(
                    cells.map(cell =>
                        cell.info.name ->
                            HashMap(cell.neighbors.map(neighbor =>
                                neighbor.name -> Math.max(
                                    0.0, 100.0 + asMap(neighbor.name).practicability - cell.practicability
                                )
                            ): _*)
                    ): _*)

            val (shp, cost) = AStarSearch.A_*(graph)(fromCell, toCell)(AStarSearch.Extractors.toList)

            log.info("Found route {} with a cost of {}", shp.mkString(" -> "), cost)

            (shp.map(s => asMap(s).info), cost)
        }
}
