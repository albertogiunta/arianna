package common

import akka.actor.{ActorSystem, Address}
import akka.cluster.ClusterEvent.{CurrentClusterState, MemberEvent, MemberRemoved, MemberUp}
import akka.cluster.{Cluster, MemberStatus}
import ontologies.{AriadneMessage, MessageType}

/**
  * This actor implements a listener for members event when nodes interact each other into the cluster
  * For example this actors is notified when the a node complete the connection to the cluster ecc...
  * Created by Matteo Gabellini on 29/06/2017.
  * Code based on akka code from official documentation
  * [link: http://doc.akka.io/docs/akka/current/scala/cluster-usage.html]
  */
class ClusterMembersListener extends CustomActor {
    
    implicit val system: ActorSystem = context.system
    
    val cluster = Cluster(system)
    
    var nodes = Set.empty[Address]
    
    override def preStart: Unit = {
        
        cluster.subscribe(self, classOf[MemberUp], classOf[MemberEvent])
        
        // If this is the master node, Actors should be already Initialized
        try {
            if (system.settings.config.getStringList("akka.cluster.seed-nodes")
                .contains(Cluster(system).selfAddress.toString)) {
                log.info("Awakening Actors on Master Actor-System")
                siblings ! AriadneMessage(MessageType.Init, "Hello there, it's time to dress-up")
            }
        } catch {
            case ex: Exception => ex.printStackTrace()
            case _: Throwable => // Ignore
        }
        
    }
    
    override def postStop: Unit =
        cluster unsubscribe self

    def receive = {
        case state: CurrentClusterState =>
            nodes = state.members.toStream
                .filter(m => m.status == MemberStatus.Up)
                .map(m => m.address).toSet
    
            log.info(nodes.toString)
            
        case MemberUp(member) =>
            //Node connected to the cluster
            nodes += member.address
            log.info("[ClusterMembersListener] Member is Up: {}. {} nodes in cluster",
                member.address, nodes.size)

            if (member.address == Cluster(system).selfAddress) {
                //init actors of current node that must interact in the cluster
                siblings ! AriadneMessage(MessageType.Init, "")
            }

        case MemberRemoved(member, _) =>
            nodes -= member.address
            log.info("[ClusterMemebersListener] Member is Removed: {}. {} nodes cluster",
                member.address, nodes.size)
        case msg => log.info("Unhandled message {} ", msg.toString)
    }
}