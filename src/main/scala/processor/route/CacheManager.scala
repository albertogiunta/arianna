package processor.route

import common.BasicActor
import ontologies.messages.Location._
import ontologies.messages.MessageType.Route
import ontologies.messages.MessageType.Route.Subtype.{Info, Response}
import ontologies.messages._

import scala.collection.mutable

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
        case msg@AriadneMessage(Route, Info, _, RouteInfo(req@RouteRequest(_, from, to), _)) =>
            
            sender ! (
                // Maybe we can check also the if there is a path saved going from "to" to "from"
                // that is a reverse path
                if (routesTimelife.get((from.name, to.name)).nonEmpty
                    && System.currentTimeMillis - routesTimelife((from.name, to.name)) < timelife) {
                    AriadneMessage(
                        Route,
                        Response,
                        Location.Cell >> Location.User,
                        RouteResponse(req, routeCache(from.name, to.name))
                    )
                } else if (routesTimelife.get((to.name, from.name)).nonEmpty
                    && System.currentTimeMillis - routesTimelife((from.name, to.name)) < timelife) {
                    AriadneMessage(
                        Route,
                        Response,
                        Location.Cell >> Location.User,
                        RouteResponse(req, routeCache(from.name, to.name).reverse)
                    )
                } else {
                    msg
                })
        
        case AriadneMessage(Route, Response, _, RouteResponse(RouteRequest(_, from, to), route)) =>
            routeCache.put((from.name, to.name), route)
            routesTimelife.put((from.name, to.name), System.currentTimeMillis)
        case _ => desist _
    }
}
