package master.core

import akka.actor.ActorSelection
import common.{BasicActor, Counter}
import ontologies.messages.Location._
import ontologies.messages.MessageType.Handshake.Subtype.Cell2Master
import ontologies.messages.MessageType.Topology.Subtype.{Planimetrics, Topology4Cell}
import ontologies.messages.MessageType.Update.Subtype.{ActualLoad, Sensors}
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
    
        case msg@AriadneMessage(Topology, Planimetrics, `admin2Server`, map: Area) =>
            log.info("A topology has been loaded in the server...")
    
            if (topology.isEmpty) {
                println(topology)
                topology = mutable.HashMap(map.cells.map(c => (c.infoCell.uri, c)): _*)
                
                context.become(behavior = sociable, discardOld = true)
                log.info("I've become Sociable...")
    
                unstashAll
        
                log.info("Notifying the Subscriber...")
                subscriber ! msg
            }
        case _ => stash
    }
    
    private def sociable: Receive = {
    
        case msg@AriadneMessage(Handshake, Cell2Master, `cell2Server`, cell: InfoCell) =>
    
            log.info(msg.toString)

            if (topology.get(cell.uri).nonEmpty) {
                log.info("Found a match into the loaded Topology for {}", cell.uri)
                topology.put(cell.uri, topology(cell.uri).copy(infoCell = cell))
                if ((synced ++) == topology.size) {
                    
                    context.become(behavior = proactive, discardOld = true)
                    log.info("I've become ProActive")
                
                    unstashAll
    
                    // Update all the Cells but first notify the subscriber
                    subscriber ! AriadneMessage(
                        Topology, Topology4Cell, server2Cell,
                        AreaForCell(Random.nextInt(),
                            topology.map(e => CellForCell(e._2)).toList)
                    )
                }
            }
        case _ => stash
    }
    
    private def proactive: Receive = {
    
        case AriadneMessage(Update, ActualLoad, `cell2Server`, pkg: ActualLoadUpdate) =>

            if (topology.get(pkg.info.uri).nonEmpty) {
                val old = topology(pkg.info.uri)

                topology.put(pkg.info.uri,
                    old.copy(
                        currentPeople = pkg.actualLoad,
                        practicabilityLevel = weight(old.capacity, pkg.actualLoad, old.passages.length)
                    )
                )
                
                // Send the updated Map to the Admin
                requestHandler ! topology.values
            }

        case AriadneMessage(Update, Sensors, `cell2Server`, pkg: SensorList) =>

            if (topology.get(pkg.info.uri).nonEmpty) {
                val news = topology(pkg.info.uri).copy(sensors = pkg.sensors)

                topology.put(pkg.info.uri, news)
                
                // Send the updated Map to the Admin
                requestHandler ! topology.values
            }
    
        case _ => desist _
    }
    
    private def weight(capacity: Int, load: Int, flows: Int): Double = {
        val log_b: (Double, Double) => Double = (b, n) => Math.log(n) / Math.log(b)
        1 / (load * 1.05 / capacity * (if (flows == 1) 0.25 else if (flows > 4.0) log_b(3.0, 4.25) else log_b(3.0, flows)))
    }
    
}