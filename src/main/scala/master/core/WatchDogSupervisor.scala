package master.core

import akka.actor.ActorRef
import com.actors.CustomActor
import com.utils.WatchDog.WatchDogNotification
import com.utils.{Counter, WatchDog}
import ontologies.messages.MessageType.Topology
import ontologies.messages.MessageType.Topology.Subtype.Acknowledgement
import ontologies.messages.{AriadneMessage, CellInfo}

import scala.collection.mutable

class WatchDogSupervisor extends CustomActor {
    
    private val actorByUri: mutable.Map[String, (ActorRef, Thread)] = mutable.HashMap.empty
    private val synced: Counter = Counter()
    
    override def receive: Receive = {
        
        case CellInfo(uri, _) if actorByUri.get(uri).isEmpty =>
            log.info("Watching {}", sender.path)
            actorByUri.put(uri, sender -> new CellWatchDog(self, uri))
            synced ++
        
        case true =>
            log.info("Starting Timers...")
            actorByUri.valuesIterator.foreach(e => if (!e._2.isAlive) e._2.start())
        
        case AriadneMessage(Topology, Acknowledgement, _, info: CellInfo) =>
            log.info("Found Topology Acknowledgement from {}", sender.path)
            if (actorByUri.get(info.uri).nonEmpty) {
    
                actorByUri.remove(info.uri)
                if ((synced --) == 0) {
                    log.info("All the Topology ACK have been received, unlocking {}", parent.path.name)
                    parent ! WatchDog.WatchDogNotification(true)
                }
            }
        
        case WatchDog.WatchDogNotification(hookedCell: String) =>
            if (actorByUri.get(hookedCell).nonEmpty) {
                val doggy = new CellWatchDog(self, hookedCell)
                actorByUri.put(hookedCell, actorByUri(hookedCell).copy(_2 = doggy))
                doggy.start()
                
                parent ! WatchDog.WatchDogNotification(actorByUri(hookedCell)._1)
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
        @volatile var eventOccurred: Boolean = false
        
        override def run(): Unit = {
            super.run()
            Thread.sleep(waitTime)
            if (!eventOccurred) actorToNotifyTimeOut ! WatchDogNotification(hookedCell)
        }
        
        override def notifyEventOccurred: Unit = eventOccurred = true
    }
}
