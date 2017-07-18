package processor.route

import common.CustomActor
import ontologies.messages._

import scala.collection.mutable

/**
  * This actor manages a cache of Route. Routes are valid for a time of X seconds
  *
  */
private class CacheManager(val timelife: Long) extends CustomActor {
    
    private val routeCache: mutable.Map[(String, String), List[InfoCell]] = mutable.HashMap.empty
    
    private val routesTimelife: mutable.Map[(String, String), Long] = mutable.HashMap.empty
    
    override def receive: Receive = {
        case msg@RouteInfo(req@RouteRequest(_, from, to), _) =>
            log.info("Evaluating cache...")
            sender ! (
                // Maybe we can check also the if there is a path saved going from "to" to "from"
                // that is a reverse path
                if (routesTimelife.get((from.name, to.name)).nonEmpty
                    && System.currentTimeMillis - routesTimelife((from.name, to.name)) < timelife) {
                    log.info("Match found in Cache...")
                    RouteResponse(req, routeCache(from.name, to.name))
                    
                } else if (routesTimelife.get((to.name, from.name)).nonEmpty
                    && System.currentTimeMillis - routesTimelife((to.name, from.name)) < timelife) {
                    log.info("Match found in Cache...")
                    RouteResponse(req, routeCache(from.name, to.name).reverse)
                    
                } else {
                    log.info("No match found in Cache...")
                    msg
                })
        
        case RouteResponse(RouteRequest(_, from, to), route) =>
            log.info("Caching new route... ")
            routeCache.put((from.name, to.name), route)
            routesTimelife.put((from.name, to.name), System.currentTimeMillis)
        case _ =>
    }
}
