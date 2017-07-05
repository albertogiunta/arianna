package master.cluster

import akka.actor.ActorSelection
import common.{BasicActor, Counter}
import ontologies.messages.Location._
import ontologies.messages.MessageType.Handshake.Subtype.Cell2Master
import ontologies.messages.MessageType.Topology.Subtype.{Planimetrics, Topology4Cell, Topology4CellLight}
import ontologies.messages.MessageType.Update.Subtype.{ActualLoad, AdminUpdate, Sensors}
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
    
    private val synched: Counter = Counter()
    
    override protected def init(args: List[Any]) = {
        log.info("Hello there from {}!", name)
        requestHandler = sibling("PINCO-PALLO").get
        publisher = sibling("Publisher-Master").get
        subscriber = sibling("Subscriber-Master").get
    }
    
    override protected def receptive = {
        
        case msg@AriadneRemoteMessage(Alarm, _, _, _) => triggerAlarm(msg)
        
        case msg@AriadneLocalMessage(Topology, Planimetrics, `admin2Server`, map: Area) =>
            log.info("A topology has been loaded in the server...")
            
            if (topology == null) {
                topology = mutable.HashMap(map.cells.map(c => (c.infoCell.name, c)): _*)
                
                context.become(behavior = sociable, discardOld = true)
                println("I've become Sociable")
                subscriber ! msg
                
                unstashAll
            }
        case _ => stash
    }
    
    private def sociable: Receive = {
        
        case msg@AriadneRemoteMessage(Alarm, _, _, _) => triggerAlarm(msg)
        
        case AriadneRemoteMessage(Handshake, Cell2Master, `cell2Server`, info: String) =>
            
            val newCell: InfoCell = Cell2Master.unmarshal(info)
            
            if (topology.get(newCell.name).nonEmpty) {
                
                topology.put(newCell.name, topology(newCell.name).copy(infoCell = newCell))
                
                if ((synched ++) == topology.size) {
                    
                    context.become(behavior = proactive, discardOld = true)
                    println("I've become ProActive")
                    
                    // Update all the Cells
                    publisher ! AriadneLocalMessage(
                        Topology, Topology4Cell, server2Cell,
                        AreaForCell(Random.nextInt(),
                            topology.map(e => CellForCell(e._2)).toList)
                    )
                }
            }
        case _ => desist _
    }
    
    private def proactive: Receive = {
        case msg@AriadneRemoteMessage(Alarm, _, _, _) => triggerAlarm(msg)
        
        case AriadneRemoteMessage(Update, ActualLoad, `cell2Server`, pkg) =>
            val up = ActualLoad.unmarshal(pkg)
            
            if (topology.get(up.info.name).nonEmpty) {
                val old = topology(up.info.name)
                val practicability = weight(old.capacity, up.actualLoad, old.passages.length)
                
                topology.put(up.info.name,
                    topology(up.info.name).copy(
                        currentPeople = up.actualLoad,
                        practicabilityLevel = practicability
                    )
                )
                
                // Send the updated Map to the Admin
                requestHandler ! AriadneLocalMessage(
                    Topology, AdminUpdate, server2Admin,
                    UpdateForAdmin(topology.values.map(c => CellUpdate(c)).toList)
                )
                
                // Update all the Cells
                publisher ! AriadneLocalMessage(
                    Topology, Topology4CellLight, server2Cell,
                    LightArea(Random.nextInt(),
                        topology.values.map(b => LightCell(b)).toList)
                )
            }
        
        case AriadneRemoteMessage(Update, Sensors, `cell2Server`, pkg) =>
            val up = Sensors.unmarshal(pkg)
            
            if (topology.get(up.info.name).nonEmpty) {
                val news = topology(up.info.name).copy(sensors = up.sensors)
                
                topology.put(up.info.name, news)
                
                // Send the updated Map to the Admin
                requestHandler ! AriadneLocalMessage(
                    Topology, AdminUpdate, server2Admin,
                    UpdateForAdmin(topology.values.map(c => CellUpdate(c)).toList)
                )
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

object TestSupervisor extends App {
    
    def calculatePracticability(capacity: Double, load: Double, flows: Double): Double = {
        val log_b: (Double, Double) => Double = (b, n) => Math.log(n) / Math.log(b)
        
        (load * 1.05) / capacity * (100.0 / log_b(4.0, flows))
    }
    
    
    println(calculatePracticability(50, 50, 2))
}