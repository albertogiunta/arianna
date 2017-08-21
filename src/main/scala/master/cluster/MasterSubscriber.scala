package master.cluster

import akka.actor.{ActorRef, ActorSelection}
import com.actors.TemplateSubscriber
import ontologies._
import ontologies.messages.Location.PreMade.cellToMaster
import ontologies.messages.Location._
import ontologies.messages.MessageType.Handshake.Subtype.{Acknowledgement, CellToMaster}
import ontologies.messages.MessageType._
import ontologies.messages._
import system.names.NamingSystem

/**
  * A Simple Subscriber for a Clustered Publish-Subscribe Model
  *
  * Created by Alessandro on 28/06/2017.
  */
class MasterSubscriber(mediator: ActorRef) extends TemplateSubscriber(mediator) {
    
    override val topics: Set[Topic[MessageContent]] = Set(Topic.TopologyACK, Topic.Updates, Topic.HandShakes)
    
    private val topologySupervisor: () => ActorSelection = () => sibling(NamingSystem.TopologySupervisor).get
    private val publisher: () => ActorSelection = () => sibling(NamingSystem.Publisher).get
    
    override protected def subscribed: Receive = {
        
        case AriadneMessage(Handshake, CellToMaster, `cellToMaster`, _) =>
            log.info("Stashing handshake from {} for later administration...", sender.path)
            publisher() ! (
                sender.path.elements.mkString("/"),
                AriadneMessage(Handshake, Acknowledgement, Location.Master >> Location.Cell, Empty())
            )
            stash

        case MasterSubscriber.TopologyLoadedACK =>
            log.info("A topology has been loaded in the server...")
            
            context.become(behavior = sociable, discardOld = true)
            log.info("I've Become Sociable...")
            log.info("Unstashing cool'n preserved Handshakes...")
            unstashAll
        
        case _ => desist _
    }
    
    def sociable: Receive = {
    
        case msg@AriadneMessage(Handshake, CellToMaster, `cellToMaster`, _) =>
            log.info("Resolving Handshake from {}", sender.path)
    
            publisher() ! (
                sender.path.elements.mkString("/"),
                AriadneMessage(Handshake, Acknowledgement, Location.Master >> Location.Cell, Empty())
            )
    
            topologySupervisor() forward msg

        case MasterSubscriber.TopologyMappedACK =>
            log.info("All the Cells have been mapped into their logical position into the Planimetry")
        
            context.become(behavior = proactive, discardOld = true)
            log.info("I've become ProActive...")
        
        case _ => desist _
    }
    
    def proactive: Receive = {
    
        case msg@AriadneMessage(Update, _, `cellToMaster`, _) =>
            log.info("Forwarding message {} from {} to TopologySupervisor with content {}", msg.subtype, sender.path, msg.content)
            topologySupervisor() forward msg

        case msg@AriadneMessage(Handshake, CellToMaster, `cellToMaster`, _) =>
            log.info("Late handshake from {}, forwarding to Supervisor...", sender.path)
            publisher() ! (
                sender.path.elements.mkString("/"),
                AriadneMessage(Handshake, Acknowledgement, Location.Master >> Location.Cell, Empty())
            )
            topologySupervisor() forward msg

        case msg@AriadneMessage(Topology, Topology.Subtype.Acknowledgement, _, _) =>
            log.info("Received Topology Acknowledgement from {}, forwarding to Supervisor...", sender.path)
            topologySupervisor() forward msg
            
        case _ => desist _
    }
}

object MasterSubscriber {
    val TopologyMappedACK: String = "TopologyMappedACK"
    val TopologyLoadedACK: String = "TopologyLoadedACK"
}