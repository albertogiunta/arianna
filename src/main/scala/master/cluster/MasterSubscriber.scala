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
    
    private val cell2Server: MessageDirection = Location.Server << Location.Cell
    private val server2Cell: MessageDirection = Location.Server >> Location.Cell
    private val admin2Server: MessageDirection = Location.Admin >> Location.Server
    
    private var topologySupervisor: ActorSelection = _
    private var publisher: ActorSelection = _
    
    override protected def init(args: List[Any]) = {
        log.info("Hello there from {}!", name)
        publisher = sibling("Publisher").get
        topologySupervisor = sibling("TopologySupervisor").get
    }
    
    override protected def receptive = {
    
        case SubscribeAck(Subscribe(topic, None, `self`)) =>
            log.info("{} Successfully Subscribed to {}", name, topic)
    
        case msg@AriadneRemoteMessage(Alarm, _, _, _) => triggerAlarm(msg)
    
        case AriadneRemoteMessage(Handshake, _, `cell2Server`, _) =>
            log.info("Stashing handshake from {} for later administration...", sender.path)
            stash
    
        case AriadneLocalMessage(Topology, Planimetrics, `admin2Server`, _) =>
            log.info("A topology has been loaded in the server...")
    
            context.become(behavior = sociable, discardOld = true)
            log.info("I've Become Sociable...")
    
            log.info("Unstashing cool'n preserved Handshakes...")
            unstashAll

        case _ => desist _
    }
    
    private def sociable: Receive = {
        
        case msg@AriadneRemoteMessage(Alarm, _, _, _) => triggerAlarm(msg)
        
        case msg@AriadneRemoteMessage(Handshake, Cell2Master, `cell2Server`, _) =>
            log.info("Resolving Handshake from {}", sender.path)
    
            topologySupervisor ! AriadneLocalMessage(
                msg.supertype,
                msg.subtype,
                msg.direction,
                Cell2Master.unmarshal(msg.content)
            )

        case msg@AriadneLocalMessage(Topology, Topology4Cell, _, _) =>
            log.info("All the Cells have been mapped into their logical position into the Planimetry")
    
            context.become(behavior = proactive, discardOld = true)
            log.info("I've become ProActive...")
    
            unstashAll
            
            publisher forward msg

        case _ => stash
    }
    
    private def proactive: Receive = {
        
        case msg@AriadneRemoteMessage(Alarm, _, _, _) => triggerAlarm(msg)

        case msg@AriadneRemoteMessage(Update, _, _, _) =>
            log.info("Forwarding message {} from {} to TopologySupervisor", msg.subtype, sender.path)

        //            topologySupervisor ! AriadneLocalMessage(
        //                msg.supertype,
        //                msg.subtype,
        //                msg.direction,
        //                ActualLoad.unmarshal(msg.content)
        //            )
        //        case msg@AriadneRemoteMessage(Update, Sensors, _, _) =>
        //            log.info("Forwarding message {} from {} to TopologySupervisor", msg.subtype, sender.path)
        //
        //            topologySupervisor ! AriadneLocalMessage(
        //                msg.supertype,
        //                msg.subtype,
        //                msg.direction,
        //                Sensors.unmarshal(msg.content)
        //            )
        case _ => desist _
    }
    
    private def triggerAlarm(msg: Message[String]) = {
        log.info("Got {} from {}", msg.toString, sender.path.name)
        // Do Your Shit
        // Non c'è bisogno di fare ciò! L'attore dell'Admin manderà l'Allarme sul
        // Sul Topic Alarm e lo riceverà anche il subscriber che informerà
        // il TopologySupervisor
        //sibling("Publisher-Master").get.forward(msg)
        // Il topologySupervisor deve contattare
        topologySupervisor ! AriadneLocalMessage(msg.supertype,
            msg.subtype,
            msg.direction,
            Alarm.Subtype.Basic.unmarshal(msg.content)
        )
    }
}