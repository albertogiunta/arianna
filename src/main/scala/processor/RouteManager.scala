package processor

import akka.actor.{ActorRef, Props}
import common.BasicActor
import ontologies.messages.Location._
import ontologies.messages.MessageType.Route
import ontologies.messages.MessageType.Route.Subtype.{Info, Response}
import ontologies.messages._
import processor.Algorithms.AStarSearch
import processor.Algorithms.Dijkstra._

import scala.collection.immutable.HashMap
import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * This Actor manages the processing of Route from a cell A to a cell B.
  *
  * It either calculates the route from scratch or retrieves it from a caching actor
  *
  * Created by Alessandro on 11/07/2017.
  */
class RouteManager extends BasicActor {
    
    private var cacher: ActorRef = _
    
    private var processor: ActorRef = _
    
    override protected def init(args: List[Any]): Unit = {
        log.info("Hello there from {}", name)
        cacher = context.actorOf(Props(new CacheManager(5000L)), "CacheManager")
        processor = context.actorOf(Props(new RouteProcessor(parent)), "RouteProcessor")
    }
    
    override protected def receptive: Receive = {
        
        case AriadneMessage(Route, Info, _, cnt: RouteInfo) =>
            
            // Se non è già presente in cache o il valore in cache è troppo vecchio
            // => Si calcola con Dijkstra il Percorso :: Si ritorna la strada in cache
            cacher ! cnt.req
            
            context.become(waitingForCache)
        case _ => desist _
    }
    
    private def waitingForCache: Receive = {
        case msg@AriadneMessage(Route, Info, _, _) if sender == cacher =>
            processor ! msg
            context.unbecome
            unstashAll
        case AriadneMessage(Route, Info, _, _) => stash
        case msg@AriadneMessage(Route, Response, _, _) =>
            parent ! msg
            context.unbecome
            unstashAll
        
        case _ => desist _
    }
}


/**
  * This Actor transform the Topology in a graph and calculate a given route using Dijkstra Algorithm
  *
  */
private class RouteProcessor(val router: ActorRef) extends BasicActor {
    
    override protected def init(args: List[Any]): Unit =
        log.info("Hello there from {}", name)
    
    override protected def receptive: Receive = {
        case AriadneMessage(Route, Info, _, RouteInfo(req, AreaForCell(_, cells))) =>
            
            Future[AriadneMessage[RouteResponse]] {
                val asMap: Map[String, CellForCell] = HashMap(cells.map(c => c.infoCell.name -> c): _*)
                
                val graph: Graph[String] =
                    HashMap(
                        cells.map(cell =>
                            cell.infoCell.name ->
                                HashMap(cell.neighbors
                                    // remove the source cell from it's respective neighbours
                                    .filter(c => c.name != req.fromCell.name).map(c =>
                                    c.name -> (100.0 + asMap(c.name).practicabilityLevel - cell.practicabilityLevel)
                                ): _*)
                        ): _*)
                
                val route: Map[String, String] =
                    AStarSearch.A_*(graph)(req.fromCell.name, req.toCell.name)
                
                AriadneMessage(
                    Route,
                    Response,
                    Location.Cell >> Location.User,
                    RouteResponse(req, null)
                )
                
            }.onComplete(promise => if (promise.isSuccess) router ! promise.get)
        
        case _ => desist _
    }
}


/**
  * This actor manages a cache of Route. Routes are valid for a time of X seconds
  *
  */
private class CacheManager(val timelife: Long) extends BasicActor {
    
    private val routeCache: mutable.Map[(String, String), List[InfoCell]] = mutable.HashMap.empty
    
    private val routesTimelife: mutable.Map[(String, String), Long] = mutable.HashMap.empty
    
    override protected def init(args: List[Any]): Unit =
        log.info("Hello there from {}", name)
    
    override protected def receptive: Receive = {
        case msg@AriadneMessage(Route, _, _, RouteInfo(req@RouteRequest(_, from, to), _)) =>
            
            sender ! (
                if (routesTimelife.get((from.name, to.name)).nonEmpty
                    && System.currentTimeMillis - routesTimelife((from.name, to.name)) < timelife) {
                    AriadneMessage(
                        Route,
                        Response,
                        Location.Cell >> Location.User,
                        RouteResponse(req, routeCache(from.name, to.name))
                    )
                } else {
                    msg
                })
        
        case (key: (String, String), route: List[InfoCell]) =>
            routeCache.put(key, route)
            routesTimelife.put(key, System.currentTimeMillis)
        case _ => desist _
    }
}
