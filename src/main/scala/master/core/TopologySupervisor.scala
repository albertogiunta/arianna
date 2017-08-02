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
    
    private var mapVersionID: Int = 0
    
    private val cell2Server: MessageDirection = Location.Cell >> Location.Master
    private val server2Cell: MessageDirection = cell2Server.reverse
    private val admin2Server: MessageDirection = Location.Admin >> Location.Master
    private val server2Admin: MessageDirection = admin2Server.reverse
    
    private val dataStreamer = context.actorOf(Props(
        new DataStreamer(target = sibling(NamingSystem.AdminManager).get)), "DataStreamer")
    
    private val publisher: () => ActorSelection = () => sibling(NamingSystem.Publisher).get
    private val subscriber: () => ActorSelection = () => sibling(NamingSystem.Subscriber).get
    
    private val synced: Counter = Counter()
    
    override def init(args: List[Any]) = {
        super.init(args)
    
        dataStreamer ! AriadneMessage(
            Error, Error.Subtype.LookingForAMap,
            server2Admin, Empty()
        )
        
    }
    
    override protected def receptive = {
    
        case msg@AriadneMessage(Topology, Planimetrics, `admin2Server`, map: Area) =>
            log.info("A topology has been loaded in the server...")
    
            if (topology.isEmpty || map.id != mapVersionID) {
    
                mapVersionID = map.id
    
                topology = mutable.HashMap(map.rooms.map(c => c.cell.info.uri -> c): _*)
                
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
                    server2Admin,
                    Empty()
                )
            }
    
        case msg@AriadneMessage(Handshake, CellToMaster, `cell2Server`, SensorsInfoUpdate(cell, sensors)) =>

            if (topology.get(cell.uri).nonEmpty) {
                log.info("Found a match into the loaded Topology for {}", cell.uri)
                val newCell = messages.Cell(cell, sensors)
                val newRoom = topology(cell.uri).copy(cell = newCell)
                topology.put(cell.uri, newRoom)
    
                dataStreamer ! msg
    
                if ((synced ++) == topology.size) {
                    
                    context.become(behavior = proactive, discardOld = true)
                    log.info("I've become ProActive")
                
                    unstashAll
    
                    // Update all the Cells but first notify the subscriber
                    subscriber() ! AriadneMessage(
                        Topology, ViewedFromACell, server2Cell,
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
                        server2Cell,
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
                    server2Admin,
                    Empty()
                )
            }
    
        case AriadneMessage(Update, CurrentPeople, `cell2Server`, pkg: CurrentPeopleUpdate) =>
    
            if (topology.get(pkg.cell.uri).nonEmpty) {
                val oldRoom = topology(pkg.cell.uri)
        
                topology.put(pkg.cell.uri,
                    oldRoom.copy(
                        currentPeople = pkg.currentPeople,
                        practicability = Practicability(oldRoom.info.capacity, pkg.currentPeople, oldRoom.passages.length)
                    )
                )
                
                // Send the updated Map to the Admin
                dataStreamer ! topology.values
            }

        case AriadneMessage(Update, Sensors, `cell2Server`, pkg: SensorsInfoUpdate) =>
    
            if (topology.get(pkg.cell.uri).nonEmpty) {
                val newCell = topology(pkg.cell.uri).cell.copy(sensors = pkg.sensors)
                val newRoom = topology(pkg.cell.uri).copy(cell = newCell)
                topology.put(pkg.cell.uri, newRoom)
                
                // Send the updated Map to the Admin
                dataStreamer ! topology.values
            }

        case AriadneMessage(Handshake, CellToMaster, `cell2Server`, _) =>
            log.info("Late handshake from {}...", sender.path)
            log.info(sender.path.name)

            publisher() ! (
                sender.path.elements.mkString("/"),
                AriadneMessage(
                    Topology, ViewedFromACell, server2Cell,
                    AreaViewedFromACell(
                        mapVersionID,
                        topology.map(e => RoomViewedFromACell(e._2)).toList)
                )
            )
            
        case _ => desist _
    }
}