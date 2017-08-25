package system.master.core

import akka.actor.ActorRef
import com.actors.TemplateActor
import com.utils.Watchdog.WatchDogNotification
import com.utils.{Counter, Watchdog}
import system.ontologies.messages.MessageType.Topology
import system.ontologies.messages.MessageType.Topology.Subtype.Acknowledgement
import system.ontologies.messages.{AriadneMessage, CellInfo}

import scala.collection.mutable

class WatchdogSupervisor extends TemplateActor {
    
    private val actorByUri: mutable.Map[String, (ActorRef, CellWatchdog)] = mutable.HashMap.empty
    private val synced: Counter = Counter()
    
    override protected def receptive: Receive = {
        
        case CellInfo(uri, _, _) if actorByUri.get(uri).isEmpty =>
            log.info("Watching {}", sender.path)
            actorByUri += uri -> (sender -> new CellWatchdog(self, uri))
            synced ++
        
        case true =>
            log.info("Starting Timers...")
            context.become(acknowledging, discardOld = true)
            actorByUri.valuesIterator.foreach(e => if (!e._2.isAlive) e._2.start())
        
        case _ =>
    }
    
    private def acknowledging: Receive = {
        case AriadneMessage(Topology, Acknowledgement, _, cell: CellInfo) =>
            log.info("Found Topology ACK for {}", sender.path.address)
            if (actorByUri.get(cell.uri).nonEmpty) {
    
                actorByUri.remove(cell.uri).get._2.notifyEventOccurred
    
                if (synced --== 0) {
                    log.info("All the Topology ACK have been received, unlocking {}", parent.path.name)
                    context.become(receptive, discardOld = true)
                
                    actorByUri.valuesIterator.foreach(p => p._2.notifyEventOccurred)
                    actorByUri.clear
        
                    parent ! Watchdog.WatchDogNotification(true)
        
                    unstashAll
                }
            }

        case Watchdog.WatchDogNotification(uri: String) =>
            log.warning("Timer for {} has expired...", uri)
            if (actorByUri.get(uri).nonEmpty) {
                val doggy = new CellWatchdog(self, uri)
                actorByUri += uri -> actorByUri(uri).copy(_2 = doggy)
                doggy.start()
                parent ! Watchdog.WatchDogNotification(actorByUri(uri)._1)
                log.info("New timer for {} has started", uri)
            }
        case _ => stash
    }
    
    /**
      *
      * @param actorToNotifyTimeOut the actor that will be notified when the time exceed
      * @param hookedCell           the cell to which this WatchDog is associated
      * @param waitTime             the time value after which the actor will be notified,
      *                             the default value is the waitTime value specified in the WatchDog companion object
      */
    class CellWatchdog(actorToNotifyTimeOut: ActorRef,
                       hookedCell: String,
                       waitTime: Long = Watchdog.waitTime) extends Thread with Watchdog {
    
        @volatile private var eventOccurred: Boolean = false
        
        override def run(): Unit = {
            super.run()
            Thread.sleep(waitTime)
            if (!eventOccurred) actorToNotifyTimeOut ! WatchDogNotification(hookedCell)
        }
        
        override def notifyEventOccurred: Unit = eventOccurred = true
    }
}
