package system.cell.cluster

import akka.cluster.Member
import com.actors.ClusterMembersListener
import system.names.NamingSystem
import system.ontologies.messages.Location._
import system.ontologies.messages.MessageType.Init
import system.ontologies.messages.{AriadneMessage, Greetings, Location}

/**
  * Supervisor of the cluster events
  * Created by Matteo Gabellini on 08/08/2017.
  */
class CellClusterSupervisor extends ClusterMembersListener {
    override protected def whenMemberUp(member: Member): Unit = {
        if (member.address == cluster.selfAddress) {
            //init actors of current node that must interact in the cluster
            log.info("Awakening Actors on Cell Actor-System")
            sibling(NamingSystem.Subscriber).get ! AriadneMessage(Init, Init.Subtype.Greetings,
                Location.Cell >> Location.Self, Greetings(List(ClusterMembersListener.greetings)))
        }
    }

    override protected def whenMemberRemoved(member: Member): Unit = {}

}
