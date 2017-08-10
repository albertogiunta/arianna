package master.core

import akka.actor.{ActorRef, ActorSelection, Props}
import com.actors.BasicActor
import com.utils.{Counter, Practicability}
import ontologies.messages
import ontologies.messages.Location._
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
    private val actorByUri: mutable.Map[String, ActorRef] = mutable.HashMap.empty
    
    private val cellToMaster: MessageDirection = Location.Cell >> Location.Master
    private val masterToCell: MessageDirection = cellToMaster.reverse
    private val adminToMaster: MessageDirection = Location.Admin >> Location.Master
    private val masterToAdmin: MessageDirection = adminToMaster.reverse
    
    private val publisher: () => ActorSelection = () => sibling(NamingSystem.Publisher).get
    private val subscriber: () => ActorSelection = () => sibling(NamingSystem.Subscriber).get
    private val admin: () => ActorSelection = () => sibling(NamingSystem.AdminManager).get
    
    private val dataStreamer = context.actorOf(Props(new DataStreamer(target = admin())), "DataStreamer")
    
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
    
        case AriadneMessage(Topology, Planimetrics, _, map: Area) =>
    
            log.error("A topology has already been loaded in the server...")
    
            if (map.id != mapVersionID) {
    
                admin() ! AriadneMessage(
                    Error,
                    Error.Subtype.MapIdentifierMismatch,
                    masterToAdmin,
                    Empty()
                )
            }

        case msg@AriadneMessage(Handshake, CellToMaster, `cellToMaster`, SensorsInfoUpdate(cell, sensors)) =>
    
            val roomName = indexByUri.get(cell.uri)
            log.info("Received handshake from cell {}", roomName)
    
            if (roomName.nonEmpty) {
                
                log.info("Found a match into the loaded Topology for {}", cell.uri)
        
                val newCell = messages.Cell(cell, sensors)
                val newRoom = topology(roomName.get).copy(cell = newCell)
        
                topology.put(roomName.get, newRoom)
                actorByUri.put(newCell.info.uri, sender)
                
                admin() ! msg
    
                if ((synced ++) == topology.size) {
                    
                    context.become(behavior = proactive, discardOld = true)
                    log.info("I've become ProActive")
                
                    unstashAll
    
                    // Update all the Cells but first notify the subscriber
                    subscriber() ! AriadneMessage(
                        Topology, ViewedFromACell, masterToCell,
                        AreaViewedFromACell(
                            mapVersionID,
                            topology.map(e => RoomViewedFromACell(e._2)).toList
                        )
                    )
                }
            } else {
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

        case AriadneMessage(Topology, Acknowledgement, `cellToMaster`, info: CellInfo) =>
    
            val cellActor = actorByUri.get(info.uri)
    
            if (cellActor.nonEmpty && cellActor.get == sender) {
        
        
                if ((synced --) == 0) {
            
                }
            }
            
        case _ => stash
    }
    
    private def proactive: Receive = {
    
        case AriadneMessage(Topology, Planimetrics, _, map: Area) =>
    
            log.error("A topology has already been loaded in the server...")
    
            if (map.id != mapVersionID) {
    
                admin() ! AriadneMessage(
                    Error,
                    Error.Subtype.MapIdentifierMismatch,
                    masterToAdmin,
                    Empty()
                )
            }

        case AriadneMessage(Update, CurrentPeople, `cellToMaster`, pkg: CurrentPeopleUpdate) =>
    
            if (topology.get(pkg.room.name).nonEmpty) {
                val oldRoom = topology(pkg.room.name)
        
                topology.put(pkg.room.name,
                    oldRoom.copy(
                        currentPeople = pkg.currentPeople,
                        practicability = Practicability(oldRoom.info.capacity, pkg.currentPeople, oldRoom.passages.length)
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

        case AriadneMessage(Handshake, CellToMaster, `cellToMaster`, _) =>
            log.info("Late handshake from {}...", sender.path)
            log.info(sender.path.name)

            publisher() ! (
                sender.path.elements.mkString("/"),
                AriadneMessage(
                    Topology, ViewedFromACell, masterToCell,
                    AreaViewedFromACell(
                        mapVersionID,
                        topology.map(e => RoomViewedFromACell(e._2)).toList)
                )
            )
            
        case _ => desist _
    }
}