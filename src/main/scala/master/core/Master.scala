package master.core

import akka.actor.{ActorRef, Props}
import com.actors.{ClusterMembersListener, CustomActor}
import master.cluster._
import ontologies.messages.AriadneMessage
import ontologies.messages.MessageType.Init

import scala.concurrent.ExecutionContext.Implicits.global
/**
  * Created by Alessandro on 29/06/2017.
  */
class Master extends CustomActor {
    
    var adminManager: ActorRef = _
    var listener: ActorRef = _
    var subscriber: ActorRef = _
    var publisher: ActorRef = _
    var topologySupervisor: ActorRef = _
    var alarmSupervisor: ActorRef = _
    
    override def preStart = {
    
        adminManager = context.actorOf(Props[AdminManager], "AdminManager")
    
        listener = context.actorOf(Props[ClusterMembersListener], "ClusterListener")
    
        subscriber = context.actorOf(Props[MasterSubscriber], "Subscriber")
    
        publisher = context.actorOf(Props[MasterPublisher], "Publisher")
    
        topologySupervisor = context.actorOf(Props[TopologySupervisor], "TopologySupervisor")
    
        alarmSupervisor = context.actorOf(Props[AlarmSupervisor], "AlarmSupervisor")
        
    }
    
    override def receive: Receive = {
        case msg@AriadneMessage(Init, Init.Subtype.Goodbyes, _, _) =>
            publisher ! msg
            context.system.terminate().onComplete(_ => println("Ariadne has shat down..."))
        case msg => log.info("Ignoring {}", msg)
    }
}

