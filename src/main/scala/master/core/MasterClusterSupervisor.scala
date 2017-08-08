package master.core

import com.actors.ClusterMembersListener
import ontologies.messages.Location._
import ontologies.messages.MessageType.Init
import ontologies.messages.{AriadneMessage, Empty, Greetings, Location}

class MasterClusterSupervisor extends ClusterMembersListener {
    
    override def preStart: Unit = {
        
        super.preStart
        
        // If this is the master node, Actors should be already Initialized
        try {
            if (config.property(builder.akka.cluster.get("seed-nodes"))
                .stringList.contains(cluster.selfAddress.toString)) {
                
                log.info("Awakening Actors on {}", cluster.selfAddress.toString)
                
                siblings ! AriadneMessage(Init, Init.Subtype.Greetings,
                    Location.Master >> Location.Self, Greetings(List(greetings)))
            }
        } catch {
            case ex: Throwable =>
                ex.printStackTrace()
                parent ! AriadneMessage(Init, Init.Subtype.Goodbyes, Location.Master >> Location.Master, Empty())
        }
    }
}
