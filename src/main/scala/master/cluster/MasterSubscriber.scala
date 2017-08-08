package master.cluster

import akka.actor.ActorSelection
import com.actors.BasicSubscriber
import ontologies._
import ontologies.messages.Location._
import ontologies.messages.MessageType.Handshake.Subtype.{Acknowledgement, CellToMaster}
import ontologies.messages.MessageType.Topology.Subtype.{Planimetrics, ViewedFromACell}
import ontologies.messages.MessageType._
import ontologies.messages._
import system.names.NamingSystem

/**
  * A Simple Subscriber for a Clustered Publish-Subscribe Model
  *
  * Created by Alessandro on 28/06/2017.
  */
class MasterSubscriber extends BasicSubscriber {
    
    override val topics: Set[Topic] = Set(Topic.Updates, Topic.HandShakes)
    
    private val cell2Server: MessageDirection = Location.Master << Location.Cell
    private val admin2Server: MessageDirection = Location.Admin >> Location.Master
    
    private val topologySupervisor: () => ActorSelection = () => sibling(NamingSystem.TopologySupervisor).get
    private val publisher: () => ActorSelection = () => sibling(NamingSystem.Publisher).get

    override protected def subscribed = {

        //        case SubscribeAck(Subscribe(topic, None, `self`)) =>
        //            log.info("{} Successfully Subscribed to {}", name, topic)

        case AriadneMessage(Handshake, CellToMaster, `cell2Server`, _) =>
            log.info("Stashing handshake from {} for later administration...", sender.path)
            stash

        case AriadneMessage(Topology, Planimetrics, `admin2Server`, _) =>
            log.info("A topology has been loaded in the server...")

            context.become(behavior = sociable, discardOld = true)
            log.info("I've Become Sociable...")
            log.info("Unstashing cool'n preserved Handshakes...")
            unstashAll

        case _ => desist _
    }
    
    def sociable: Receive = {
    
        case msg@AriadneMessage(Handshake, CellToMaster, `cell2Server`, _) =>
            log.info("Resolving Handshake from {}", sender.path)
            publisher() ! AriadneMessage(Handshake, Acknowledgement, Location.Master >> Location.Cell, Empty())
            //                (
            //                sender.path.elements.mkString("/"),
            //                AriadneMessage(Handshake, Acknowledgement, Location.Master >> Location.Cell, Empty())
            //            )
            topologySupervisor() forward msg
    
        case msg@AriadneMessage(Topology, ViewedFromACell, _, _) =>
            log.info("All the Cells have been mapped into their logical position into the Planimetry")
    
            context.become(behavior = proactive, discardOld = true)
            log.info("I've become ProActive...")
    
            unstashAll
    
            publisher() forward msg

        case _ => stash
    }
    
    def proactive: Receive = {
    
        case msg@AriadneMessage(Update, _, _, _) =>
            log.info("Forwarding message {} from {} to TopologySupervisor", msg.subtype, sender.path)
            topologySupervisor() forward msg

        case msg@AriadneMessage(Handshake, CellToMaster, `cell2Server`, _) =>
            log.info("Late handshake from {}... Forwarding to Supervisor...", sender.path)
            publisher() ! AriadneMessage(Handshake, Acknowledgement, Location.Master >> Location.Cell, Empty())
            //                (
            //                sender.path.elements.mkString("/"),
            //                AriadneMessage(Handshake, Acknowledgement, Location.Master >> Location.Cell, Empty())
            //            )
            topologySupervisor() forward msg
        
        case _ => desist _
    }
}