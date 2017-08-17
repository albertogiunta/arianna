package com.actors

import akka.actor.Address
import akka.cluster.ClusterEvent.{CurrentClusterState, MemberEvent, MemberRemoved, MemberUp}
import akka.cluster.{Cluster, Member, MemberStatus}

/**
  * This actor implements a listener for members event when nodes interact each other into the cluster
  * For example this actors is notified when the a node complete the connection to the cluster ecc...
  *
  * Created by Matteo Gabellini on 29/06/2017.
  * Code based on akka code from official documentation
  * [link: http://doc.akka.io/docs/akka/current/scala/cluster-usage.html]
  */
abstract class ClusterMembersListener extends CustomActor {
    
    protected val cluster: Cluster = Cluster(context.system)
    
    protected var nodes: Set[Address] = Set.empty
    
    override def preStart: Unit = {
        cluster.subscribe(self, classOf[MemberUp], classOf[MemberEvent])
        log.info("Started...")
    }
    
    override def postStop: Unit = cluster unsubscribe self
    
    def receive: Receive = {
        case state: CurrentClusterState =>
            nodes = state.members.toStream
                .filter(m => m.status == MemberStatus.Up)
                .map(m => m.address).toSet
            //Check if the current node is already up
            //in order to complete the cell initialization
            val currentMember = state.members.toStream
                .filter(m => m.status == MemberStatus.Up)
                .map(m => (m.address, m))
                .toMap.get(cluster.selfAddress)
            if (currentMember.isDefined) whenMemberUp(currentMember.get)


        case MemberUp(member) =>
            //Node connected to the cluster
            nodes += member.address
            log.info("Member is Up: {}. {} nodes in cluster",
                member.address, nodes.size)
    
            whenMemberUp(member)


        case MemberRemoved(member, _) =>
            nodes -= member.address
            log.info("Member is Removed: {}. {} nodes cluster",
                member.address, nodes.size)
            whenMemberRemoved(member)
        
        case msg => log.info("Unhandled... {} ", msg.toString)
    }


    protected def whenMemberUp(member: Member): Unit

    protected def whenMemberRemoved(member: Member): Unit
}

object ClusterMembersListener {

    val greetings: String = "Hello there, it's time to dress-up"

}