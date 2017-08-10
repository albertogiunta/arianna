package master.core

import akka.actor.{ActorRef, ActorSelection, Props}
import com.actors.BasicActor
import com.utils.WatchDog.WatchDogNotification
import com.utils.{Counter, Practicability}
import ontologies.messages
import ontologies.messages.Location.PreMade._
import ontologies.messages.MessageType.Handshake.Subtype.CellToMaster
import ontologies.messages.MessageType.Topology.Subtype.{Acknowledgement, Planimetrics, ViewedFromACell}
import ontologies.messages.MessageType.Update.Subtype.{CurrentPeople, Sensors}
import ontologies.messages.MessageType.{Error, Handshake, Topology, Update}
import ontologies.messages._
import system.names.NamingSystem

import scala.collection.mutable


/**
  * This Actor has the main duty to maintain the Topology Updated with the incoming information
  * and to distribute the updated map to the Cells and the Admin
  *
  * Created by Alessandro on 05/07/2017.
  */
class TopologySupervisor extends BasicActor {
    
    private var mapVersionID: Int = 0
    
    private var topology: mutable.Map[String, Room] = mutable.HashMap.empty
    private var indexByUri: mutable.Map[String, String] = mutable.HashMap.empty
    
    private val alreadyMapped: mutable.Set[String] = mutable.HashSet.empty
    
    private val publisher: () => ActorSelection = () => sibling(NamingSystem.Publisher).get
    private val subscriber: () => ActorSelection = () => sibling(NamingSystem.Subscriber).get
    private val admin: () => ActorSelection = () => sibling(NamingSystem.AdminManager).get
    
    private val dataStreamer = context.actorOf(Props(new DataStreamer(target = admin())), NamingSystem.DataStreamer)
    private val watchDogSupervisor = context.actorOf(Props[WatchDogSupervisor], NamingSystem.WatchDogSupervisor)
    
    private val synced: Counter = Counter()
    
    override def init(args: List[Any]): Unit = {
    
        super.init(args)
    
        admin() ! AriadneMessage(
            Error, Error.Subtype.LookingForAMap,
            masterToAdmin, Empty()
        )
        
    }
    
    override protected def receptive: Receive = {
        
        case msg@AriadneMessage(Topology, Planimetrics, `adminToMaster`, map: Area) =>
            log.info("A topology has been loaded in the server...")
    
            if (topology.isEmpty || map.id != mapVersionID) {
    
                mapVersionID = map.id
    
                topology = mutable.HashMap(map.rooms.map(room => room.info.id.name -> room): _*)
                indexByUri = mutable.HashMap(map.rooms.map(room => room.cell.info.uri -> room.info.id.name): _*)
                
                context.become(behavior = sociable, discardOld = true)
                log.info("I've become Sociable...")
    
                unstashAll
        
                log.info("Notifying the Subscriber...")
                subscriber() ! msg
            }

        case _ => stash
    }
    
    private def sociable: Receive = {
    
        case AriadneMessage(Topology, Planimetrics, _, map: Area) => unexpectedPlanimetry(map)
        
        case msg@AriadneMessage(Handshake, CellToMaster, `cellToMaster`, SensorsInfoUpdate(cell, sensors)) =>
    
            log.info("Received handshake from cell {}", cell.uri)
    
            if (indexByUri.get(cell.uri).nonEmpty && !alreadyMapped(cell.uri)) {
                
                log.info("Found a match into the loaded Topology for {}", cell.uri)
        
                val newCell = messages.Cell(cell, sensors)
                val newRoom = topology(indexByUri(cell.uri)).copy(cell = newCell)
        
                topology.put(indexByUri(cell.uri), newRoom)
                alreadyMapped.add(cell.uri)
                
                admin() ! msg
        
                watchDogSupervisor forward cell
                
                if ((synced ++) == topology.size) {
    
                    context.become(acknowledging, discardOld = true)
                    log.info("I've Become Acknowledging!")
                    // Update all the Cells but first notify the subscriber
                    subscriber() ! AriadneMessage(
                        Topology, ViewedFromACell, masterToCell,
                        AreaViewedFromACell(
                            mapVersionID,
                            topology.map(e => RoomViewedFromACell(e._2)).toList
                        )
                    )
    
                    log.info("Notifying the watchdog supervisor to run timers...")
                    watchDogSupervisor ! true
                }
        
            } else if (!alreadyMapped(cell.uri)) {
                log.error("Received Handshake as no matching in the current loaded Topology for {}", cell.uri)
                publisher() ! (
                    sender.path.elements.mkString("/"),
                    AriadneMessage(
                        Error, Error.Subtype.CellMappingMismatch,
                        masterToCell,
                        Empty()
                    )
                )
            }
    
        case _ => stash
    }
    
    private def acknowledging: Receive = {
        
        case AriadneMessage(Topology, Planimetrics, _, map: Area) => unexpectedPlanimetry(map)
        
        case msg@AriadneMessage(Topology, Acknowledgement, `cellToMaster`, _) =>
            watchDogSupervisor forward msg
        
        case WatchDogNotification(true) =>
            context.become(proactive)
            log.info("I've become ProActive")
            unstashAll
        
        case WatchDogNotification(hookedActor: ActorRef) =>
            
            log.info("Resending new Topology to {}", hookedActor.path)
            
            publisher() ! (
                hookedActor.path.elements.mkString("/"),
                AriadneMessage(
                    Topology, ViewedFromACell, masterToCell,
                    AreaViewedFromACell(
                        mapVersionID,
                        topology.map(e => RoomViewedFromACell(e._2)).toList
                    )
                )
            )
        
        case _ => stash
    }
    
    private def proactive: Receive = {
    
        case AriadneMessage(Topology, Planimetrics, _, map: Area) => unexpectedPlanimetry(map)
        
        case AriadneMessage(Update, CurrentPeople, `cellToMaster`, pkg: CurrentPeopleUpdate) =>
    
            if (topology.get(pkg.room.name).nonEmpty) {
                val oldRoom = topology(pkg.room.name)
        
                topology.put(pkg.room.name,
                    oldRoom.copy(
                        currentPeople = pkg.currentPeople,
                        practicability = Practicability(
                            oldRoom.info.capacity, pkg.currentPeople, oldRoom.passages.length
                        )
                    )
                )
                
                // Send the updated Map to the Admin
                dataStreamer ! topology.values
            }
    
        case AriadneMessage(Update, Sensors, `cellToMaster`, pkg: SensorsInfoUpdate) =>
        
            if (topology.get(pkg.cell.uri).nonEmpty) {
                val newCell = topology(pkg.cell.uri).cell.copy(sensors = pkg.sensors)
                val newRoom = topology(pkg.cell.uri).copy(cell = newCell)
                topology.put(pkg.cell.uri, newRoom)
                
                // Send the updated Map to the Admin
                dataStreamer ! topology.values
            }
    
        case AriadneMessage(Handshake, CellToMaster, `cellToMaster`, SensorsInfoUpdate(cell, _)) =>
            log.info("Late handshake from {}...", sender.path)
        
            context.unbecome()
        
            watchDogSupervisor forward cell
    
            publisher() ! (
                sender.path.elements.mkString("/"),
                AriadneMessage(
                    Topology, ViewedFromACell, masterToCell,
                    AreaViewedFromACell(
                        mapVersionID,
                        topology.map(e => RoomViewedFromACell(e._2)).toList)
                )
            )
        
            watchDogSupervisor ! "Start"
        
        case _ => desist _
    }
    
    
    private def unexpectedPlanimetry(map: Area): Unit = {
        log.error("A topology has already been loaded in the server...")
        if (map.id != mapVersionID) {
            
            admin() ! AriadneMessage(
                Error,
                Error.Subtype.MapIdentifierMismatch,
                masterToAdmin,
                Empty()
            )
        }
    }
}