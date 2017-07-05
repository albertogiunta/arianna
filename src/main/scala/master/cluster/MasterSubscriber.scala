package master.cluster

import akka.actor.ActorSelection
import akka.cluster.pubsub.DistributedPubSubMediator.{Subscribe, SubscribeAck}
import common.BasicSubscriber
import ontologies._
import ontologies.messages.Location._
import ontologies.messages.MessageType.Handshake.Subtype.Cell2Master
import ontologies.messages.MessageType.Topology.Subtype.{Planimetrics, Topology4Cell}
import ontologies.messages.MessageType._
import ontologies.messages._

/**
  * A Simple Subscriber for a Clustered Publish-Subscribe Model
  *
  * Created by Alessandro on 28/06/2017.
  */
class MasterSubscriber extends BasicSubscriber {
    
    override val topics: Set[Topic] = Set(Topic.Alarm, Topic.Update, Topic.HandShake)
    
    private val cell2Server: MessageDirection = Server << Location.Cell
    private val server2Cell: MessageDirection = Server >> Location.Cell
    private val admin2Server: MessageDirection = Admin >> Server
    
    private var topologySupervisor: ActorSelection = _
    private var publisher: ActorSelection = _
    
    override protected def init(args: List[Any]) = {
        log.info("Hello there from {}!", name)
        publisher = sibling("Publisher-Master").get
        topologySupervisor = sibling("TopologySupervisor").get
    }
    
    override protected def receptive = {
    
        case SubscribeAck(Subscribe(topic, None, `self`)) =>
            log.info("{} Successfully Subscribed to {}", name, topic)
    
        case msg@AriadneRemoteMessage(Alarm, _, _, _) => triggerAlarm(msg)
    
        case AriadneRemoteMessage(Handshake, _, `cell2Server`, _) =>
            log.info("Stashing handshake for later administration...")
            stash
    
        case AriadneLocalMessage(Topology, Planimetrics, `admin2Server`, _) =>
            log.info("A topology has been loaded in the server...")
    
            context.become(behavior = sociable, discardOld = true)
    
            unstashAll
        case _ => desist _
    }
    
    private def sociable: Receive = {
        
        case msg@AriadneRemoteMessage(Alarm, _, _, _) => triggerAlarm(msg)
        
        case msg@AriadneRemoteMessage(Handshake, Cell2Master, `cell2Server`, _) =>
            log.info("Resolving Handshake from {}", sender.path)
    
            topologySupervisor forward msg
        
        case msg@AriadneLocalMessage(Topology, Topology4Cell, `server2Cell`, _) =>
            log.info("All the Cells have been mapped into their logical position into the Planimetry")
            
            context.become(behavior = active, discardOld = true)
    
            publisher forward msg
        
        case _ => desist _
    }
    
    private def active: Receive = {
        
        case msg@AriadneRemoteMessage(Alarm, _, _, _) => triggerAlarm(msg)

        case msg@AriadneRemoteMessage(Update, _, `cell2Server`, _) =>
            log.info("Forwarding message {} from {} to TopologySupervisor", msg.subtype, sender.path)
    
            topologySupervisor forward msg
        
        case _ => desist _
    }
    
    private def triggerAlarm(msg: Message[_]) = {
        log.info("Got {} from {}", msg.toString, sender.path.name)
        // Do Your Shit
        // Non c'è bisogno di fare ciò! L'attore dell'Admin manderà l'Allarme sul
        // Sul Topic Alarm e lo riceverà anche il subscriber che informerà
        // il TopologySupervisor
        //sibling("Publisher-Master").get.forward(msg)
        // Il topologySupervisor deve contattare
        topologySupervisor forward msg
    }
}