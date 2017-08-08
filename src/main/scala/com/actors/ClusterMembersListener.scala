package com.actors

import akka.actor.{ActorRef, Address}
import akka.cluster.ClusterEvent.{CurrentClusterState, MemberEvent, MemberRemoved, MemberUp}
import akka.cluster.pubsub.DistributedPubSub
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

    //    protected val greetings: String = "Hello there, it's time to dress-up"

    protected val cluster = Cluster(context.system)

    protected val mediator: ActorRef = DistributedPubSub(context.system).mediator

    protected var nodes = Set.empty[Address]

    override def preStart = {
        cluster.subscribe(self, classOf[MemberUp], classOf[MemberEvent])
    }
    
    override def postStop: Unit = cluster unsubscribe self

    def receive = {
        case state: CurrentClusterState =>
            nodes = state.members.toStream
                .filter(m => m.status == MemberStatus.Up)
                .map(m => m.address).toSet

        case MemberUp(member) =>
            //Node connected to the cluster
            nodes += member.address
            log.info("Member is Up: {}. {} nodes in cluster",
                member.address, nodes.size)
            Thread.sleep(5000)
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