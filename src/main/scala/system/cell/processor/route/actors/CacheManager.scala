package system.cell.processor.route.actors

import com.actors.CustomActor
import system.ontologies.messages._

import scala.collection.mutable

/**
  * This actor manages a cache of the Routes. Routes are valid for a time of X seconds
  *
  */
class CacheManager(val cacheKeepAlive: Long) extends CustomActor {
    
    private val routeCache: mutable.Map[(String, String), List[RoomID]] = mutable.HashMap.empty
    
    private val routesTimelife: mutable.Map[(String, String), Long] = mutable.HashMap.empty
    
    override def receive: Receive = {
        case msg@RouteInfo(req@RouteRequest(_, from, to, _), _) =>
            log.info("Evaluating cache...")
    
            sender ! (
                if (routesTimelife.get((from.name, to.name)).nonEmpty
                    && System.currentTimeMillis - routesTimelife((from.name, to.name)) < cacheKeepAlive) {
                    log.info("Match found in Cache...")
                    RouteResponse(req, routeCache(from.name, to.name))
                    
                } else if (routesTimelife.get((to.name, from.name)).nonEmpty
                    && System.currentTimeMillis - routesTimelife((to.name, from.name)) < cacheKeepAlive) {
                    log.info("Match found in Cache...")
                    RouteResponse(req, routeCache(from.name, to.name).reverse)
                    
                } else {
                    log.info("No match found in Cache...")
                    msg
                })

        case RouteResponse(RouteRequest(_, from, to, _), route) =>
            log.info("Caching new route... ")
            routeCache.put((from.name, to.name), route)
            routesTimelife.put((from.name, to.name), System.currentTimeMillis)
        case _ =>
    }
}
