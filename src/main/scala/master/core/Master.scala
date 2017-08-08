package master.core

import akka.actor.{ActorRef, Props}
import com.actors.CustomActor
import master.cluster._
import ontologies.messages.AriadneMessage
import ontologies.messages.MessageType.Init

import scala.concurrent.ExecutionContext.Implicits.global
/**
  * Created by Alessandro on 29/06/2017.
  */
class Master extends CustomActor {
    
    var subscriber: ActorRef = _
    var publisher: ActorRef = _
    var adminManager: ActorRef = _
    var topologySupervisor: ActorRef = _
    var alarmSupervisor: ActorRef = _
    var listener: ActorRef = _
    
    override def preStart = {
    
        subscriber = context.actorOf(Props[MasterSubscriber], "Subscriber")
    
        publisher = context.actorOf(Props[MasterPublisher], "Publisher")
    
        adminManager = context.actorOf(Props[AdminManager], "AdminManager")
    
        topologySupervisor = context.actorOf(Props[TopologySupervisor], "TopologySupervisor")
    
        alarmSupervisor = context.actorOf(Props[AlarmSupervisor], "AlarmSupervisor")
    
        listener = context.actorOf(Props[MasterClusterSupervisor], "ClusterListener")
        
    }
    
    override def receive: Receive = {
    
        case msg@AriadneMessage(Init, Init.Subtype.Goodbyes, _, _) =>
            publisher ! msg
            context.system.terminate().onComplete(_ => println("Ariadne has shat down..."))
    
        case msg => log.info("Ignoring {}", msg)
    }
}

