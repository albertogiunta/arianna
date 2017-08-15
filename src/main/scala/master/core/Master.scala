package master.core

import akka.actor.{ActorRef, Props}
import com.actors.CustomActor
import master.cluster._
import ontologies.messages.AriadneMessage
import ontologies.messages.MessageType.Init
import system.names.NamingSystem

import scala.concurrent.ExecutionContext.Implicits.global
/**
  * Created by Alessandro on 29/06/2017.
  */
class Master(mediator: ActorRef) extends CustomActor {
    
    private var subscriber: ActorRef = _
    private var publisher: ActorRef = _
    private var adminManager: ActorRef = _
    private var topologySupervisor: ActorRef = _
    private var alarmSupervisor: ActorRef = _
    private var listener: ActorRef = _
    
    override def preStart: Unit = {
    
        super.preStart()
    
        subscriber = context.actorOf(Props(new MasterSubscriber(mediator)), NamingSystem.Subscriber)
    
        publisher = context.actorOf(Props(new MasterPublisher(mediator)), NamingSystem.Publisher)
    
        alarmSupervisor = context.actorOf(Props(new AlarmSupervisor(mediator)), NamingSystem.AlarmSupervisor)
    
        adminManager = context.actorOf(Props[AdminManager], NamingSystem.AdminManager)
    
        topologySupervisor = context.actorOf(Props[TopologySupervisor], NamingSystem.TopologySupervisor)
    
        listener = context.actorOf(Props[MasterClusterSupervisor], NamingSystem.ClusterSupervisor)
        
    }
    
    override def receive: Receive = {
    
        case msg@AriadneMessage(Init, Init.Subtype.Goodbyes, _, _) =>
            publisher ! msg
            context.system.terminate().onComplete(_ => println("Ariadne has shat down..."))
    
        case msg => log.info("Ignoring {}", msg)
    }
}

