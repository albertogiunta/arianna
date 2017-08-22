package system.master.cluster

import akka.actor.ActorRef
import akka.cluster.pubsub.DistributedPubSubMediator._
import com.actors.TemplatePublisher
import system.names.NamingSystem
import system.ontologies.Topic
import system.ontologies.messages.MessageType.Handshake.Subtype.Acknowledgement
import system.ontologies.messages.MessageType._
import system.ontologies.messages._

/**
  * Created by Alessandro on 28/06/2017.
  */
class MasterPublisher(mediator: ActorRef) extends TemplatePublisher(mediator) {
    
    override protected def receptive: Receive = {
    
        case msg@AriadneMessage(Init, Init.Subtype.Goodbyes, _, _) =>
            log.info("Forwarding system shut-down... ")
            mediator ! Publish(Topic.ShutDown, msg)
            
        case msg@AriadneMessage(Alarm, _, _, _) =>
            log.info("Forwarding... {}", msg)
            mediator ! Publish(Topic.Alarms, msg)
        
        case msg@AriadneMessage(Topology, _, _, _) =>
            log.info("Forwarding... {}", msg)
            mediator ! Publish(Topic.Topologies, msg)
    
        case msg@AriadneMessage(Handshake, Acknowledgement, _, _) =>
            log.info("Forwarding... {}", msg)
            mediator ! Publish(Topic.HandShakes, msg)
            
        case (dest: String, cnt: AriadneMessage[_]) =>
    
            val target = "/" + dest.replace(NamingSystem.Publisher, NamingSystem.Subscriber)
    
            log.info("Forwarding Point to Point message {} to {}", cnt.subtype.toString, target)
    
            mediator ! Send(target, cnt, localAffinity = false)
            
        case _ => desist _
    }
}