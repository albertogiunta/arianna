package master.core

import akka.actor.ActorSelection
import com.actors.BasicActor
import com.utils.{Counter, Practicability}
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
    
    private var topology: mutable.Map[String, Cell] = mutable.HashMap.empty
    
    private var mapVersionID: Int = 0
    
    private val cell2Server: MessageDirection = Location.Cell >> Location.Master
    private val server2Cell: MessageDirection = cell2Server.reverse
    private val admin2Server: MessageDirection = Location.Admin >> Location.Master
    private val server2Admin: MessageDirection = admin2Server.reverse
    
    private val dataStreamer: () => ActorSelection = () => sibling(NamingSystem.DataStreamer).get
    private val publisher: () => ActorSelection = () => sibling(NamingSystem.Publisher).get
    private val subscriber: () => ActorSelection = () => sibling(NamingSystem.Subscriber).get
    
    private val synced: Counter = Counter()
    
    override protected def receptive = {
    
        case msg@AriadneMessage(Topology, Planimetrics, `admin2Server`, map: Area) =>
            log.info("A topology has been loaded in the server...")
    
            if (topology.isEmpty || map.id != mapVersionID) {
    
                mapVersionID = map.id
                
                topology = mutable.HashMap(map.cells.map(c => (c.info.uri, c)): _*)
                
                context.become(behavior = sociable, discardOld = true)
                log.info("I've become Sociable...")
    
                unstashAll
        
                log.info("Notifying the Subscriber...")
                subscriber() ! msg
            }

        case _ => stash
    }
    
    private def sociable: Receive = {
    
        case msg@AriadneMessage(Topology, Planimetrics, _, map: Area) =>
        
        
            if (map.id != mapVersionID) {
                log.error("A topology has already been loaded in the server...")
            
                dataStreamer() ! AriadneMessage(
                    Error,
                    Error.Subtype.MapIdentifierMismatch,
                    server2Admin,
                    Empty()
                )
            }
        
        case msg@AriadneMessage(Handshake, CellToMaster, `cell2Server`, SensorsInfoUpdate(cell, _)) =>

            if (topology.get(cell.uri).nonEmpty) {
                log.info("Found a match into the loaded Topology for {}", cell.uri)
                topology.put(cell.uri, topology(cell.uri).copy(info = cell))
    
                dataStreamer() ! msg
                
                if ((synced ++) == topology.size) {
                    
                    context.become(behavior = proactive, discardOld = true)
                    log.info("I've become ProActive")
                
                    unstashAll
    
                    // Update all the Cells but first notify the subscriber
                    subscriber() ! AriadneMessage(
                        Topology, ViewedFromACell, server2Cell,
                        AreaViewedFromACell(
                            mapVersionID,
                            topology.map(e => CellViewedFromACell(e._2)).toList
                        )
                    )
                }
            } else {
                log.error("Received Handshake as no matching in the current loaded Topology for {}", cell.uri)
                publisher() ! (
                    sender.path.elements.mkString("/")
                        .replace(NamingSystem.Publisher, NamingSystem.Subscriber),
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
            
                dataStreamer() ! AriadneMessage(
                    Error,
                    Error.Subtype.MapIdentifierMismatch,
                    server2Admin,
                    Empty()
                )
            }
    
        case AriadneMessage(Update, CurrentPeople, `cell2Server`, pkg: CurrentPeopleUpdate) =>

            if (topology.get(pkg.info.uri).nonEmpty) {
                val old = topology(pkg.info.uri)

                topology.put(pkg.info.uri,
                    old.copy(
                        currentPeople = pkg.currentPeople,
                        practicability = Practicability(old.capacity, pkg.currentPeople, old.passages.length)
                    )
                )
                
                // Send the updated Map to the Admin
                dataStreamer() ! topology.values
            }

        case AriadneMessage(Update, Sensors, `cell2Server`, pkg: SensorsInfoUpdate) =>

            if (topology.get(pkg.info.uri).nonEmpty) {
                val news = topology(pkg.info.uri).copy(sensors = pkg.sensors)

                topology.put(pkg.info.uri, news)
                
                // Send the updated Map to the Admin
                dataStreamer() ! topology.values
            }

        case AriadneMessage(Handshake, CellToMaster, `cell2Server`, _) =>
            log.info("Late handshake from {}...", sender.path)
    
            publisher() ! (
                sender.path.elements.mkString("/")
                    .replace(NamingSystem.Publisher, NamingSystem.Subscriber),
                AriadneMessage(
                    Topology, ViewedFromACell, server2Cell,
                    AreaViewedFromACell(
                        mapVersionID,
                        topology.map(e => CellViewedFromACell(e._2)).toList)
                )
            )
            
        case _ => desist _
    }
}