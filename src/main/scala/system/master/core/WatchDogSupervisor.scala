package system.master.core

import akka.actor.ActorRef
import com.actors.CustomActor
import com.utils.WatchDog.WatchDogNotification
import com.utils.{Counter, WatchDog}
import system.ontologies.messages.MessageType.Topology
import system.ontologies.messages.MessageType.Topology.Subtype.Acknowledgement
import system.ontologies.messages.{AriadneMessage, CellInfo}

import scala.collection.mutable

class WatchDogSupervisor extends CustomActor {
    
    private val actorByUri: mutable.Map[String, (ActorRef, CellWatchDog)] = mutable.HashMap.empty
    private val synced: Counter = Counter()
    
    override def receive: Receive = {
    
        case CellInfo(uri, _, _) if actorByUri.get(uri).isEmpty =>
            log.info("Watching {}", sender.path)
            actorByUri.put(uri, sender -> new CellWatchDog(self, uri))
            synced ++
        
        case true =>
            log.info("Starting Timers...")
            actorByUri.valuesIterator.foreach(e => if (!e._2.isAlive) e._2.start())

        case AriadneMessage(Topology, Acknowledgement, _, cell: CellInfo) =>
            log.info("Found Topology ACK for {}", sender.path.address)
            if (actorByUri.get(cell.uri).nonEmpty) {
        
                actorByUri.remove(cell.uri).get._2.notifyEventOccurred
        
                if (synced --== 0) {
                    log.info("All the Topology ACK have been received, unlocking {}", parent.path.name)
                    parent ! WatchDog.WatchDogNotification(true)
            
                    actorByUri.valuesIterator.foreach(p => p._2.notifyEventOccurred)
                    actorByUri.clear
                }
            }

        case WatchDog.WatchDogNotification(uri: String) =>
            log.warning("Timer for {} has expired...", uri)
            if (actorByUri.get(uri).nonEmpty) {
                val doggy = new CellWatchDog(self, uri)
                actorByUri.put(uri, actorByUri(uri).copy(_2 = doggy))
                doggy.start()
        
                parent ! WatchDog.WatchDogNotification(actorByUri(uri)._1)
            }
        case _ =>
    }
    
    /**
      *
      * @param actorToNotifyTimeOut the actor that will be notified when the time exceed
      * @param hookedCell           the cell to which this WatchDog is associated
      * @param waitTime             the time value after which the actor will be notified,
      *                             the default value is the waitTime value specified in the WatchDog companion object
      */
    class CellWatchDog(actorToNotifyTimeOut: ActorRef,
                       hookedCell: String,
                       waitTime: Long = WatchDog.waitTime) extends Thread with WatchDog {
    
        @volatile private var eventOccurred: Boolean = false
        
        override def run(): Unit = {
            super.run()
            Thread.sleep(waitTime)
            if (!eventOccurred) actorToNotifyTimeOut ! WatchDogNotification(hookedCell)
        }
        
        override def notifyEventOccurred: Unit = eventOccurred = true
    }
}
