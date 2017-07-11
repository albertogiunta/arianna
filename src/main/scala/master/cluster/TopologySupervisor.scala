package master.cluster

import akka.actor.ActorSelection
import common.{BasicActor, Counter}
import ontologies.messages.Location._
import ontologies.messages.MessageType.Handshake.Subtype.Cell2Master
import ontologies.messages.MessageType.Topology.Subtype.{Planimetrics, Topology4Cell, Topology4CellLight}
import ontologies.messages.MessageType.Update.Subtype.{ActualLoad, Sensors}
import ontologies.messages.MessageType.{Alarm, Handshake, Topology, Update}
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
    
    private var requestHandler: ActorSelection = _
    private var publisher: ActorSelection = _
    private var subscriber: ActorSelection = _
    
    private val synced: Counter = Counter()
    
    override protected def init(args: List[Any]) = {
        log.info("Hello there from {}!", name)
        requestHandler = sibling("DataStreamer").get
        publisher = sibling("Publisher").get
        subscriber = sibling("Subscriber").get
    }
    
    override protected def receptive = {
    
        case msg@AriadneLocalMessage(Alarm, _, _, _) => triggerAlarm(msg)
        
        case msg@AriadneLocalMessage(Topology, Planimetrics, `admin2Server`, map: Area) =>
            log.info("A topology has been loaded in the server...")
    
            if (topology.isEmpty) {
                topology = mutable.HashMap(map.cells.map(c => (c.infoCell.name, c)): _*)
                
                context.become(behavior = sociable, discardOld = true)
                log.info("I've become Sociable...")
    
                unstashAll
        
                log.info("Notifying the Subscriber...")
                subscriber ! msg
            }
        case _ => stash
    }
    
    private def sociable: Receive = {
    
        case msg@AriadneLocalMessage(Alarm, _, _, _) => triggerAlarm(msg)
    
        case AriadneLocalMessage(Handshake, Cell2Master, `cell2Server`, cell: InfoCell) =>
        
            if (topology.get(cell.name).nonEmpty) {
                log.info("Found a match into the loaded Topology for {}", cell.name)
                topology.put(cell.name, topology(cell.name).copy(infoCell = cell))
            
                if ((synced ++) == topology.size) {
                    
                    context.become(behavior = proactive, discardOld = true)
                    log.info("I've become ProActive")
                
                    unstashAll
    
                    // Update all the Cellsbut first notify the subscriber
                    subscriber ! AriadneLocalMessage(
                        Topology, Topology4Cell, server2Cell,
                        AreaForCell(Random.nextInt(),
                            topology.map(e => CellForCell(e._2)).toList)
                    )
                }
            }
        case _ => stash
    }
    
    private def proactive: Receive = {
    
        case msg@AriadneLocalMessage(Alarm, _, _, _) => triggerAlarm(msg)
    
        case AriadneLocalMessage(Update, ActualLoad, `cell2Server`, pkg: ActualLoadUpdate) =>
        
            if (topology.get(pkg.info.name).nonEmpty) {
                val old = topology(pkg.info.name)
            
                topology.put(pkg.info.name,
                    topology(pkg.info.name).copy(
                        currentPeople = pkg.actualLoad,
                        practicabilityLevel = weight(old.capacity, pkg.actualLoad, old.passages.length)
                    )
                )
                
                // Send the updated Map to the Admin
                requestHandler ! topology.values
    
                //                // Update all the Cells -- Cells Updates their references by themselves
                //                publisher ! AriadneLocalMessage(
                //                    Topology, Topology4CellLight, server2Cell,
                //                    LightArea(Random.nextInt(),
                //                        topology.values.map(b => LightCell(b)).toList)
                //                )
            }
    
        case AriadneLocalMessage(Update, Sensors, `cell2Server`, pkg: SensorList) =>
        
            if (topology.get(pkg.info.name).nonEmpty) {
                val news = topology(pkg.info.name).copy(sensors = pkg.sensors)
    
                topology.put(pkg.info.name, news)
                
                // Send the updated Map to the Admin
                requestHandler ! topology.values
            }
    
        case _ => desist _
    }
    
    private def triggerAlarm(msg: Message[_]): Unit = {
        // Update all the Cells
        publisher ! AriadneLocalMessage(
            Topology, Topology4CellLight, server2Cell,
            LightArea(Random.nextInt(),
                topology.values.map(b => LightCell(b)).toList)
        )
    }
    
    private def weight(capacity: Int, load: Int, flows: Int): Double = {
        val log_b: (Double, Double) => Double = (b, n) => Math.log(n) / Math.log(b)
        (load * 1.05) / capacity * (100.0 / log_b(4.0, flows))
    }
    
}