package system.master.cluster

import akka.cluster.Member
import com.actors.ClusterMembersListener
import system.names.NamingSystem
import system.ontologies.messages.Location._
import system.ontologies.messages.MessageType.Init
import system.ontologies.messages.{AriadneMessage, Empty, Greetings, Location}

class MasterClusterSupervisor extends ClusterMembersListener {
    
    override def preStart: Unit = {
        
        super.preStart
    
        // If this is the system.master node, Actors should be already Initialized
        try {
            if (configManager.property(builder.akka.cluster.get("seed-nodes"))
                .asStringList.contains(cluster.selfAddress.toString)) {

                log.info("Awakening Actors on {}", cluster.selfAddress.toString)
    
                sibling(NamingSystem.Subscriber).get ! AriadneMessage(Init, Init.Subtype.Greetings,
                    Location.Master >> Location.Self, Greetings(List(ClusterMembersListener.greetings)))
            }
        } catch {
            case ex: Throwable =>
                ex.printStackTrace()
                parent ! AriadneMessage(Init, Init.Subtype.Goodbyes, Location.Master >> Location.Master, Empty())
        }
    }

    override protected def whenMemberUp(member: Member): Unit = {}

    override protected def whenMemberRemoved(member: Member): Unit = {}
}