package master.core

import akka.actor.{ActorSelection, Props}
import com.actors.BasicActor
import com.utils.{Counter, Practicability}
import ontologies.messages
import ontologies.messages.Location._
import ontologies.messages.MessageType.Handshake.Subtype.CellToMaster
import ontologies.messages.MessageType.Topology.Subtype.{Planimetrics, ViewedFromACell}
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
    
    private var topology: mutable.Map[String, Room] = mutable.HashMap.empty
    private var indexByUri: mutable.Map[String, String] = mutable.HashMap.empty
    
    private var mapVersionID: Int = 0
    
    private val cell2Master: MessageDirection = Location.Cell >> Location.Master
    private val master2Cell: MessageDirection = cell2Master.reverse
    private val admin2Master: MessageDirection = Location.Admin >> Location.Master
    private val master2Admin: MessageDirection = admin2Master.reverse
    
    private val dataStreamer = context.actorOf(Props(
        new DataStreamer(target = sibling(NamingSystem.AdminManager).get)), "DataStreamer")
    
    private val publisher: () => ActorSelection = () => sibling(NamingSystem.Publisher).get
    private val subscriber: () => ActorSelection = () => sibling(NamingSystem.Subscriber).get
    
    private val synced: Counter = Counter()
    
    override def init(args: List[Any]) = {
    
        super.init(args)
    
        dataStreamer ! AriadneMessage(
            Error, Error.Subtype.LookingForAMap,
            master2Admin, Empty()
        )
        
    }
    
    override protected def receptive = {
    
        case msg@AriadneMessage(Topology, Planimetrics, `admin2Master`, map: Area) =>
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
        
            if (map.id != mapVersionID) {
                log.error("A topology has already been loaded in the server...")
    
                dataStreamer ! AriadneMessage(
                    Error,
                    Error.Subtype.MapIdentifierMismatch,
                    master2Admin,
                    Empty()
                )
            }

        case msg@AriadneMessage(Handshake, CellToMaster, `cell2Master`, SensorsInfoUpdate(cell, sensors)) =>
    
            val roomName = indexByUri.get(cell.uri)
            log.info("Received handshake from cell {}", roomName)
    
            if (roomName.nonEmpty) {
                
                log.info("Found a match into the loaded Topology for {}", cell.uri)
        
                val newCell = messages.Cell(cell, sensors)
                val newRoom = topology(roomName.get).copy(cell = newCell)
        
                topology.put(roomName.get, newRoom)
    
                dataStreamer ! msg
    
                if ((synced ++) == topology.size) {
                    
                    context.become(behavior = proactive, discardOld = true)
                    log.info("I've become ProActive")
                
                    unstashAll
    
                    // Update all the Cells but first notify the subscriber
                    subscriber() ! AriadneMessage(
                        Topology, ViewedFromACell, master2Cell,
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
                        master2Cell,
                        Empty()
                    )
                )
            }
    
        case _ => stash
    }
    
    private def proactive: Receive = {
    
        case AriadneMessage(Topology, Planimetrics, _, map: Area) =>
        
            if (map.id != mapVersionID) {
                log.error("A topology has already been loaded in the server...")
    
                dataStreamer ! AriadneMessage(
                    Error,
                    Error.Subtype.MapIdentifierMismatch,
                    master2Admin,
                    Empty()
                )
            }

        case AriadneMessage(Update, CurrentPeople, `cell2Master`, pkg: CurrentPeopleUpdate) =>
    
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

        case AriadneMessage(Update, Sensors, `cell2Master`, pkg: SensorsInfoUpdate) =>
    
            if (topology.get(pkg.cell.uri).nonEmpty) {
                val newCell = topology(pkg.cell.uri).cell.copy(sensors = pkg.sensors)
                val newRoom = topology(pkg.cell.uri).copy(cell = newCell)
                topology.put(pkg.cell.uri, newRoom)
                
                // Send the updated Map to the Admin
                dataStreamer ! topology.values
            }

        case AriadneMessage(Handshake, CellToMaster, `cell2Master`, _) =>
            log.info("Late handshake from {}...", sender.path)
            log.info(sender.path.name)

            publisher() ! (
                sender.path.elements.mkString("/"),
                AriadneMessage(
                    Topology, ViewedFromACell, master2Cell,
                    AreaViewedFromACell(
                        mapVersionID,
                        topology.map(e => RoomViewedFromACell(e._2)).toList)
                )
            )
            
        case _ => desist _
    }
}