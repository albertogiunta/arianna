package processor.route

import akka.actor.{ActorRef, Props}
import common.BasicActor
import ontologies.messages.Location._
import ontologies.messages.MessageType.Route
import ontologies.messages.MessageType.Route.Subtype.{Escape, Info, Response}
import ontologies.messages._

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
    
    
    override def preStart() = {
        super.preStart()
        cacher = context.actorOf(Props(new CacheManager(5000L)), "CacheManager")
        processor = context.actorOf(Props(new RouteProcessor(parent)), "RouteProcessor")
    }
    
    override protected def init(args: List[Any]): Unit = {
        log.info("Hello there from {}", name)
    }
    
    override protected def receptive: Receive = {
    
        case AriadneMessage(Route, Escape.Request, _, cnt: EscapeRequest) => manageEscape(cnt)
    
        case msg@AriadneMessage(Route, Info, _, _) =>
            
            // Se non è già presente in cache o il valore in cache è troppo vecchio
            // => Si calcola con Dijkstra il Percorso :: Si ritorna la strada in cache
            log.info("Requesting route from Cache...")
            context.become(waitingForCache, discardOld = true)
            cacher ! msg.content
            
        case _ => desist _
    }
    
    private def waitingForCache: Receive = {
        case AriadneMessage(Route, Escape.Request, _, cnt: EscapeRequest) => manageEscape(cnt)
    
        case AriadneMessage(Route, Info, _, _) => stash
    
        case cnt@RouteInfo(_, _) if sender == cacher =>
            log.info("No cached route is present, sending data to Processor...")
            processor ! cnt
            context.become(receptive, discardOld = true)
            unstashAll
    
        case cnt@RouteResponse(_, _) if sender == cacher =>
            log.info("A valid cached route is present, sending data to Core...")
            parent ! AriadneMessage(
                Route,
                Response,
                Location.Cell >> Location.User,
                cnt
            )
            context.become(receptive, discardOld = true)
            unstashAll
    
        case _ => desist _
    }
    
    private def evacuating: Receive = {
        case AriadneMessage(Route, Escape.Response, _, _: EscapeResponse) =>
        case _ => stash
    }
    
    def manageEscape(cnt: EscapeRequest): Unit = {
        log.info("Escape route request received, becoming evacuating...")
        context.become(evacuating)
        
        processor forward cnt
    }
}