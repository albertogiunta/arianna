package system.master.cluster

import akka.actor.{ActorRef, ActorSelection}
import com.actors.TemplateSubscriber
import system.names.NamingSystem
import system.ontologies._
import system.ontologies.messages.Location.PreMade.cellToMaster
import system.ontologies.messages.Location._
import system.ontologies.messages.MessageType.Handshake.Subtype.{Acknowledgement, CellToMaster}
import system.ontologies.messages.MessageType._
import system.ontologies.messages._

import scala.collection.mutable

/**
  * A Simple Subscriber for a Clustered Publish-Subscribe Model
  *
  * Created by Alessandro on 28/06/2017.
  */
class MasterSubscriber(mediator: ActorRef) extends TemplateSubscriber(mediator) {
    
    override val topics: Set[Topic[MessageContent]] = Set(Topic.TopologyACK, Topic.Updates, Topic.HandShakes)
    
    private val topologySupervisor: () => ActorSelection = () => sibling(NamingSystem.TopologySupervisor).get
    private val publisher: () => ActorSelection = () => sibling(NamingSystem.Publisher).get
    
    private val stashedHandshakes: mutable.Set[String] = mutable.HashSet.empty
    
    override protected def subscribed: Receive = {
    
        case AriadneMessage(Handshake, CellToMaster, `cellToMaster`, pkg: SensorsInfoUpdate) =>
    
            publisher() ! HACK()
        
            if (stashedHandshakes.apply(pkg.cell.uri)) {
                log.info("Stashing handshake from {} for later administration...", sender.path)
                stashedHandshakes.add(pkg.cell.uri)
                stash
            } else {
                log.info("Handshakes from {} already stashed...", sender.path)
            }

        case MasterSubscriber.TopologyLoadedACK =>
            
            context.become(behavior = sociable, discardOld = true)
            log.info("I've Become Sociable...")
    
            unstashAll
        
        case _ => desist _
    }
    
    def sociable: Receive = {
    
        case msg@AriadneMessage(Handshake, CellToMaster, `cellToMaster`, _) =>
    
            publisher() ! HACK()
            topologySupervisor() forward msg

        case MasterSubscriber.TopologyMappedACK =>
            context.become(behavior = proactive, discardOld = true)
            log.info("I've become ProActive...")
        
        case _ => desist _
    }
    
    def proactive: Receive = {
    
        case msg@AriadneMessage(Update, _, `cellToMaster`, _) =>
            topologySupervisor() forward msg

        case msg@AriadneMessage(Handshake, CellToMaster, `cellToMaster`, _) =>
            publisher() ! HACK()
            topologySupervisor() forward msg

        case msg@AriadneMessage(Topology, Topology.Subtype.Acknowledgement, _, _) =>
            topologySupervisor() forward msg
            
        case _ => desist _
    }
    
    private val HACK: () => (String, AriadneMessage[MessageContent]) = () => (
        sender.path.elements.mkString("/"),
        AriadneMessage(Handshake, Acknowledgement, Location.Master >> Location.Cell, Empty())
    )
}

object MasterSubscriber {
    val TopologyMappedACK: String = "TopologyMappedACK"
    val TopologyLoadedACK: String = "TopologyLoadedACK"
}