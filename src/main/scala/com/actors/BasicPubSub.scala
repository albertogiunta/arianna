package com.actors

import akka.actor.{Actor, ActorRef}
import akka.cluster.pubsub.DistributedPubSubMediator._
import ontologies.Topic
import ontologies.messages.Location._
import ontologies.messages.MessageType.Init
import ontologies.messages.{AriadneMessage, Greetings, Location}

/**
  * This class gives a common template for a Akka Subscriber.
  *
  * The actual implementation of this Template is partly provided by the BasicActor superclass,
  * partly by the topics abstract val, where must be placed the topics to which the Actor need to be subscribed.
  *
  * The subscription is automatically done during the preStart phase.
  *
  * Created by Alessandro on 01/07/2017.
  */
abstract class BasicSubscriber(mediator: ActorRef) extends BasicActor {
    
    val cluster = akka.cluster.Cluster(context.system)

    protected val topics: Set[Topic] // To Override Necessarily
    private var ackTopicReceived: Integer = 0

    override protected def init(args: List[Any]): Unit = {
        super.init(args)
        mediator ! Put(self) // Point 2 Point Messaging with other Actors of the cluster
        topics.foreach(topic => mediator ! Subscribe(topic, self))
    }

    override protected def receptive = {
        case SubscribeAck(Subscribe(topic, _, me)) if me == self =>
            log.info("{} Successfully Subscribed to {}", name, topic)
            ackTopicReceived = ackTopicReceived + 1
        
            if (ackTopicReceived == topics.size) {
                this.context.become(subscribed, discardOld = true)
            
                siblings ! AriadneMessage(Init, Init.Subtype.Greetings,
                    Location.Cell >> Location.Self, Greetings(List(ClusterMembersListener.greetings)))
                log.info("I've become Subscribed!")
    
                unstashAll
            }

        case _: AriadneMessage[_] => stash
        case _ => desist _
    }

    protected def subscribed: Actor.Receive = ???
}

/**
  * This class gives a common template for a Akka Publisher
  *
  */
abstract class BasicPublisher(mediator: ActorRef) extends BasicActor {
    
    val cluster = akka.cluster.Cluster(context.system)

    // Point 2 Point Messaging with other Actors of the cluster
    override protected def init(args: List[Any]): Unit = {
        super.init(args)
        mediator ! Put(self) // Point 2 Point Messaging with other Actors of the cluster
    }

}