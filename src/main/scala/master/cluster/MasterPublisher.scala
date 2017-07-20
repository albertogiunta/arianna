package master.cluster

import akka.cluster.pubsub.DistributedPubSubMediator._
import common.BasicPublisher
import ontologies.Topic
import ontologies.messages.MessageType._
import ontologies.messages._

/**
  * Created by Alessandro on 28/06/2017.
  */
class MasterPublisher extends BasicPublisher {
    
    override protected def init(args: List[Any]) = {
        log.info("Hello there from {}!", name)
    }
    
    override protected def receptive = {
        
        case msg@AriadneMessage(Alarm, _, _, _) =>
            log.info("Forwarding... {}", msg)
            mediator ! Publish(Topic.Alarms, msg)
        
        case msg@AriadneMessage(Topology, _, _, _) =>
            log.info("Forwarding... {}", msg)
            mediator ! Publish(Topic.Topologies, msg)
        
        case (dest: String, cnt: AriadneMessage[_]) =>
            log.info("Forwarding Point to Point message {} to {}", cnt.toString, dest)
            mediator ! Send(dest.replace("Publisher", "Subscriber"), cnt, localAffinity = false)
            
        case _ => desist _
    }
}