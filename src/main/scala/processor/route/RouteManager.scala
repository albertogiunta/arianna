package processor.route

import akka.actor.{ActorRef, Props}
import common.BasicActor
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
        case msg@AriadneMessage(Route, Escape.Request, _, _: EscapeRequest) => manageEscape(msg)
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
        case msg@AriadneMessage(Route, Escape.Request, _, _: EscapeRequest) => manageEscape(msg)
        case _ => desist _
    }
    
    private def evaquating: Receive = {
        case AriadneMessage(Route, Escape.Response, _, _: EscapeResponse) =>
            ???
        case _ => desist _
    }
    
    def manageEscape(msg: AriadneMessage[MessageContent]): Unit = {
        context.become(evaquating)
        
        processor forward msg
    }
}