package system.master.core

import akka.actor.{ActorRef, ActorSelection, Props}
import com.actors.TemplateActor
import com.utils.Counter
import com.utils.WatchDog.WatchDogNotification
import system.master.cluster.MasterSubscriber
import system.names.NamingSystem
import system.ontologies.messages.Location.PreMade._
import system.ontologies.messages.MessageType.Handshake.Subtype.CellToMaster
import system.ontologies.messages.MessageType.Topology.Subtype.{Acknowledgement, Planimetrics, ViewedFromACell}
import system.ontologies.messages.MessageType.Update.Subtype.{CurrentPeople, Sensors}
import system.ontologies.messages.MessageType.{Error, Handshake, Topology, Update}
import system.ontologies.messages.SensorsInfoUpdate._
import system.ontologies.messages._

import scala.collection.mutable


/**
  * This Actor has the main duty to maintain the Topology Updated with the incoming information
  * and to distribute the updated map to the Cells and the Admin
  *
  * Created by Alessandro on 05/07/2017.
  */
class TopologySupervisor extends TemplateActor {
    
    private var mapVersionID: Int = 0
    
    private val topology: mutable.Map[String, Room] = mutable.HashMap.empty
    private val indexByUri: mutable.Map[String, String] = mutable.HashMap.empty
    private val alreadyMapped: mutable.Set[String] = mutable.HashSet.empty
    
    private val publisher: () => ActorSelection = () => sibling(NamingSystem.Publisher).get
    private val subscriber: () => ActorSelection = () => sibling(NamingSystem.Subscriber).get
    private val admin: () => ActorSelection = () => sibling(NamingSystem.AdminSupervisor).get
    
    private var dataStreamer: ActorRef = _
    private var watchDogSupervisor: ActorRef = _
    
    private val synced: Counter = Counter()
    
    override def preStart: Unit = {
        super.preStart
        dataStreamer = context.actorOf(Props(new DataStreamer(target = admin())), NamingSystem.DataStreamer)
        watchDogSupervisor = context.actorOf(Props[WatchDogSupervisor], NamingSystem.WatchDogSupervisor)
    }
    
    protected override def init(args: List[String]): Unit = {
    
        super.init(args)
        log.info("Requesting map to the Admin Application...")
        
        admin() ! AriadneMessage(
            Error, Error.Subtype.LookingForAMap,
            masterToAdmin, Empty()
        )
    }
    
    override protected def receptive: Receive = {
    
        case AriadneMessage(Topology, Planimetrics, `adminToMaster`, map: Area) =>
            log.info("A topology has been loaded in the server...")
    
            if (topology.isEmpty || map.id != mapVersionID) {
    
                mapVersionID = map.id
    
                map.rooms.foreach(room => {
                    topology.put(room.info.id.name, room)
                    indexByUri.put(room.cell.info.uri, room.info.id.name)
                })
                
                context.become(behavior = sociable, discardOld = true)
                log.info("I've become Sociable...")
        
                log.info("Notifying the Subscriber...")
                subscriber() ! MasterSubscriber.TopologyLoadedACK
    
                log.info("Sending Topology ACK to Admin...")
                admin() ! AriadneMessage(Topology, Acknowledgement, masterToAdmin, CellInfo.empty)
    
                unstashAll
            }

        case _ => stash
    }
    
    private def sociable: Receive = {
    
        case AriadneMessage(Topology, Planimetrics, _, map: Area) => unexpectedPlanimetry(map)

        case msg@AriadneMessage(Handshake, CellToMaster, `cellToMaster`, cnt@SensorsInfoUpdate(cell, _)) =>
    
            log.info("Received handshake from cell {}", cell.uri)
    
            if (indexByUri.get(cell.uri).nonEmpty && !alreadyMapped(cell.uri)) {
        
                log.info("Found a match into the loaded Topology for {}", cell.uri)
    
                val newRoom = topology(indexByUri(cell.uri)).copy(cell = cnt)
        
                topology.put(indexByUri(cell.uri), newRoom)
                alreadyMapped.add(cell.uri)
    
                admin() forward msg
        
                watchDogSupervisor forward cell
    
                if (synced ++== topology.size) {
                    log.info("All the Cells have been mapped into their logical position into the Planimetry")
                    context.become(acknowledging, discardOld = true)
                    log.info("I've Become Acknowledging!")
    
                    subscriber() ! MasterSubscriber.TopologyMappedACK
    
                    publisher() ! AriadneMessage(
                        Topology, ViewedFromACell, masterToCell,
                        AreaViewedFromACell(mapVersionID, topology.map(e => RoomViewedFromACell(e._2)).toList)
                    )
    
                    log.info("Notifying the watchdog supervisor to run timers...")
                    watchDogSupervisor ! true
                }
        
            } else if (!alreadyMapped(cell.uri)) {
                log.error("Received Handshake has no matching in the current loaded Topology for {}", cell.uri)
                publisher() ! (
                    sender.path.elements.mkString("/"),
                    AriadneMessage(Error, Error.Subtype.CellMappingMismatch, masterToCell, Empty())
                )
            }

        case _ => desist _
    }
    
    private def acknowledging: Receive = {
        
        case AriadneMessage(Topology, Planimetrics, _, map: Area) => unexpectedPlanimetry(map)
        
        case msg@AriadneMessage(Topology, Acknowledgement, `cellToMaster`, _) =>
            log.info("Received Topology Acknowledgement from {}, forwarding to W.D.Supervisor...", sender.path.address)
            watchDogSupervisor forward msg
        
        case WatchDogNotification(true) =>
            context.become(proactive, discardOld = true)
            log.info("I've become ProActive")
            unstashAll
        
        case WatchDogNotification(hookedActor: ActorRef) =>
            log.info("Resending new Topology to {}", hookedActor.path.address)
            
            publisher() ! (
                hookedActor.path.elements.mkString("/"),
                AriadneMessage(
                    Topology, ViewedFromACell, masterToCell,
                    AreaViewedFromACell(mapVersionID, topology.map(e => RoomViewedFromACell(e._2)).toList)
                )
            )

        case _ => stash
    }
    
    private def proactive: Receive = {
    
        case AriadneMessage(Topology, Planimetrics, _, map: Area) => unexpectedPlanimetry(map)
        
        case AriadneMessage(Update, CurrentPeople, `cellToMaster`, pkg: CurrentPeopleUpdate) =>
    
            if (topology.get(pkg.room.name).nonEmpty) {
                log.info("Updating current people in {} from {}", pkg.room.name, sender.path.address)
                val newRoom = topology(pkg.room.name).copy(currentPeople = pkg.currentPeople)
                topology.put(pkg.room.name, newRoom)
                
                dataStreamer ! topology.values
            }

        case AriadneMessage(Update, Sensors, `cellToMaster`, SensorsInfoUpdate(cell, sensors)) =>
    
            if (topology.get(indexByUri(cell.uri)).nonEmpty) {
                log.info("Updating sensor values in {} from {}", cell.uri, sender.path.address)
                val newCell = topology(indexByUri(cell.uri)).cell.copy(sensors = sensors)
                val newRoom = topology(indexByUri(cell.uri)).copy(cell = newCell)
                topology.put(indexByUri(cell.uri), newRoom)
                dataStreamer ! topology.values
            }
    
        case AriadneMessage(Handshake, CellToMaster, `cellToMaster`, SensorsInfoUpdate(cell, _)) =>
            log.info("Late handshake from {}...", sender.path.address)
            context.become(acknowledging, discardOld = true)
            
            watchDogSupervisor forward cell
    
            publisher() ! (
                sender.path.elements.mkString("/"),
                AriadneMessage(
                    Topology, ViewedFromACell, masterToCell,
                    AreaViewedFromACell(mapVersionID, topology.map(e => RoomViewedFromACell(e._2)).toList)
                )
            )
            watchDogSupervisor ! true
        
        case _ => desist _
    }
    
    private def unexpectedPlanimetry(map: Area): Unit = {
    
        if (map.id != mapVersionID) {
            log.error("Received an unexpected Topology from {} but Map ID mismatch ...", sender.path.address)
            admin() ! AriadneMessage(
                Error,
                Error.Subtype.MapIdentifierMismatch,
                masterToAdmin,
                Empty()
            )
        } else {
            log.error("Received an unexpected Topology from {}, synchronizing...", sender.path.address)
    
            admin() ! AriadneMessage(Topology, Acknowledgement, masterToAdmin, CellInfo.empty)
            
            topology.valuesIterator.foreach(room =>
                admin() ! AriadneMessage(Handshake, CellToMaster, cellToMaster, SensorsInfoUpdate(room))
            )
        }
    }
}