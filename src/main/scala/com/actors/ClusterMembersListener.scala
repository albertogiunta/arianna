package com.actors

import akka.actor.Address
import akka.cluster.ClusterEvent.{CurrentClusterState, MemberEvent, MemberRemoved, MemberUp}
import akka.cluster.{Cluster, MemberStatus}
import ontologies.messages.Location._
import ontologies.messages.MessageType.Init
import ontologies.messages.{AriadneMessage, Greetings, Location}

/**
  * This actor implements a listener for members event when nodes interact each other into the cluster
  * For example this actors is notified when the a node complete the connection to the cluster ecc...
  *
  * Created by Matteo Gabellini on 29/06/2017.
  * Code based on akka code from official documentation
  * [link: http://doc.akka.io/docs/akka/current/scala/cluster-usage.html]
  */
class ClusterMembersListener extends CustomActor {

    private val greetings: String = "FROM CLUSTER MEMBERS LISTENER Hello there, it's time to dress-up"

    private val cluster = Cluster(context.system)

    private var nodes = Set.empty[Address]

    override def preStart = {

        cluster.subscribe(self, classOf[MemberUp], classOf[MemberEvent])

        // If this is the master node, Actors should be already Initialized
        try {
            if (config.property(builder.akka.cluster.get("seed-nodes"))
                .stringList.contains(cluster.selfAddress.toString)) {

                log.info("Awakening Actors on Master Actor-System")

                siblings ! AriadneMessage(Init, Init.Subtype.Greetings,
                    Location.Master >> Location.Self, Greetings(List(greetings)))
            }
        } catch {
            case ex: Throwable => ex.printStackTrace()
        }
    }

    override def postStop: Unit =
        cluster unsubscribe self

    def receive = {
        case state: CurrentClusterState =>
            nodes = state.members.toStream
                .filter(m => m.status == MemberStatus.Up)
                .map(m => m.address).toSet

        case MemberUp(member) =>
            //Node connected to the cluster
            nodes += member.address
            log.info("[ClusterMembersListener] Member is Up: {}. {} nodes in cluster",
                member.address, nodes.size)

            if (member.address == cluster.selfAddress) {
                //init actors of current node that must interact in the cluster
                log.info("Awakening Actors on Cell Actor-System")
                siblings ! AriadneMessage(Init, Init.Subtype.Greetings,
                    Location.Cell >> Location.Self, Greetings(List(greetings)))
            }

        case MemberRemoved(member, _) =>
            nodes -= member.address
            log.info("[ClusterMemebersListener] Member is Removed: {}. {} nodes cluster",
                member.address, nodes.size)
        case msg => log.info("Unhandled... {} ", msg.toString)
    }
}