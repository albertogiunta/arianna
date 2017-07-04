package cell.cluster.test

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Publish, Put, Send}
import ontologies.Topic
import ontologies.messages.AriadneRemoteMessage
import ontologies.messages.MessageType._

/**
  * A generic publisher actor to test the message delivery to CellSubscriber actor in the cluster
  * Created by Matteo Gabellini on 29/06/2017 based on a Alessandro Cevoli's publisher.
  */
class TestPublisher extends Actor with ActorLogging {

    // activate the extension
    private val mediator: ActorRef = DistributedPubSub(context.system).mediator


    override def preStart() = {
        mediator ! Put(self) // Point 2 Point Messaging with other Actors of the cluster
    }

    def receive = {
    
        case AriadneRemoteMessage(Init, Init.Subtype.Basic, dir, cnt) =>
            println("[" + self.path.name + "] Hello there from {}!", self.path.name)

            println("[" + self.path.name + "] I've become receptive!")

            val topicName = Topic.Alarm.toString
            mediator ! Publish(topicName,
                AriadneRemoteMessage(Alarm, Alarm.Subtype.Basic, dir, cnt))

            println(s"[" + self.path.name + "] Message published on " + Topic.Alarm)

            // The Mediator Hierarchy is always /user/<Username>
            val subName = "Subscriber1"
            mediator ! Send(path = "/user/" + subName,
                msg = AriadneRemoteMessage(Topology, Topology.Subtype.Topology4User, dir, cnt + "2"),
                localAffinity = true)

            println(s"[" + self.path.name + "] Message sent directly to " + subName)

        case _ => // Ignore
    }
}