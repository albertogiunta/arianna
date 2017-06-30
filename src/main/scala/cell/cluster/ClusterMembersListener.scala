package cell.cluster

import akka.actor.{Actor, ActorLogging, ActorRef, Address}
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
class ClusterMembersListener(val actorsToInitialize: List[ActorRef]) extends Actor with ActorLogging {

    implicit val system = context.system
    val cluster = Cluster(system)

    override def preStart(): Unit =
        cluster.subscribe(self, classOf[MemberUp], classOf[MemberEvent])

    override def postStop(): Unit =
        cluster unsubscribe self

    var nodes = Set.empty[Address]

    def receive = {
        case state: CurrentClusterState =>
            nodes = state.members.collect {
                case m if m.status == MemberStatus.Up => m.address
            }
        case MemberUp(member) =>
            //Node connected to the cluster
            nodes += member.address
            log.info("[ClusterMembersListener] Member is Up: {}. {} nodes in cluster",
                member.address, nodes.size)

            if (member.address == Cluster(system).selfAddress) {
                //init actors of current node that must interact in the cluster
                actorsToInitialize.foreach(X => {
                    X ! AriadneMessage(MessageType.Init, "")
                })
            }
        case MemberRemoved(member, _) =>
            nodes -= member.address
            log.info("[ClusterMemebersListener] Member is Removed: {}. {} nodes cluster",
                member.address, nodes.size)
        case _: MemberEvent => // ignore
    }
}
