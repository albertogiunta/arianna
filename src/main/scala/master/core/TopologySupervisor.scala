package master.core

import akka.actor.ActorSelection
import common.BasicActor
import common.utils.{Counter, Practicability}
import ontologies.messages.Location._
import ontologies.messages.MessageType.Handshake.Subtype.Cell2Master
import ontologies.messages.MessageType.Topology.Subtype.{Planimetrics, Topology4Cell}
import ontologies.messages.MessageType.Update.Subtype.{CurrentPeople, Sensors}
import ontologies.messages.MessageType.{Handshake, Topology, Update}
import ontologies.messages._

import scala.collection.mutable
import scala.util.Random


/**
  * This Actor has the main duty to maintain the Topology Updated with the incoming information
  * and to distribute the updated map to the Cells and the Admin
  *
  * Created by Alessandro on 05/07/2017.
  */
class TopologySupervisor extends BasicActor {
    
    private var topology: mutable.Map[String, Cell] = mutable.HashMap.empty
    
    private val cell2Server: MessageDirection = Location.Cell >> Location.Server
    private val server2Cell: MessageDirection = cell2Server.reverse
    private val admin2Server: MessageDirection = Location.Admin >> Location.Server
    private val server2Admin: MessageDirection = admin2Server.reverse
    
    private val dataStreamer: () => ActorSelection = () => sibling("DataStreamer").get
    private val publisher: () => ActorSelection = () => sibling("Publisher").get
    private val subscriber: () => ActorSelection = () => sibling("Subscriber").get
    
    private val synced: Counter = Counter()
    
    override protected def receptive = {
    
        case msg@AriadneMessage(Topology, Planimetrics, `admin2Server`, map: Area) =>
            log.info("A topology has been loaded in the server...")
    
            if (topology.isEmpty) {
                println(topology)
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
    
        case msg@AriadneMessage(Handshake, Cell2Master, `cell2Server`, SensorList(cell, _)) =>
    
            log.info(msg.toString)

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
                        Topology, Topology4Cell, server2Cell,
                        AreaForCell(
                            Random.nextInt,
                            topology.map(e => CellForCell(e._2)).toList
                        )
                    )
                }
            }
        case _ => stash
    }
    
    private def proactive: Receive = {
    
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

        case AriadneMessage(Update, Sensors, `cell2Server`, pkg: SensorList) =>

            if (topology.get(pkg.info.uri).nonEmpty) {
                val news = topology(pkg.info.uri).copy(sensors = pkg.sensors)

                topology.put(pkg.info.uri, news)
                
                // Send the updated Map to the Admin
                dataStreamer() ! topology.values
            }

        case AriadneMessage(Handshake, Cell2Master, `cell2Server`, _) =>
            log.info("Late handshake from {}...", sender.path)
    
            publisher() ! (
                sender.path.toString,
                AriadneMessage(
                    Topology, Topology4Cell, server2Cell,
                    AreaForCell(
                        Random.nextInt,
                        topology.map(e => CellForCell(e._2)).toList)
                )
            )
            
        case _ => desist _
    }
}