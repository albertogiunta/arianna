package system.master

import akka.actor.{ActorRef, Props}
import com.actors.CustomActor
import system.master.cluster._
import system.master.core.{AdminSupervisor, TopologySupervisor}
import system.names.NamingSystem
import system.ontologies.messages.AriadneMessage
import system.ontologies.messages.MessageType.Init

import scala.concurrent.ExecutionContext.Implicits.global
/**
  * Created by Alessandro on 29/06/2017.
  */
class Master(mediator: ActorRef) extends CustomActor {
    
    var subscriber: ActorRef = _
    var publisher: ActorRef = _
    var adminManager: ActorRef = _
    var topologySupervisor: ActorRef = _
    var alarmSupervisor: ActorRef = _
    var listener: ActorRef = _
    
    override def preStart: Unit = {
    
        super.preStart()
    
        subscriber = context.actorOf(Props(new MasterSubscriber(mediator)), NamingSystem.Subscriber)
    
        publisher = context.actorOf(Props(new MasterPublisher(mediator)), NamingSystem.Publisher)
    
        alarmSupervisor = context.actorOf(Props(new AlarmSupervisor(mediator)), NamingSystem.AlarmSupervisor)

        adminManager = context.actorOf(Props[AdminSupervisor], NamingSystem.AdminSupervisor)
    
        topologySupervisor = context.actorOf(Props[TopologySupervisor], NamingSystem.TopologySupervisor)
    
        listener = context.actorOf(Props[MasterClusterSupervisor], NamingSystem.ClusterSupervisor)
        
    }
    
    override def receive: Receive = {
    
        case msg@AriadneMessage(Init, Init.Subtype.Goodbyes, _, _) =>
            publisher ! msg
            context.system.terminate().onComplete(_ => {
                println("Ariadne has shat down...");
                System.exit(1)
            })
    
        case msg => log.info("Ignoring {}", msg)
    }
}

