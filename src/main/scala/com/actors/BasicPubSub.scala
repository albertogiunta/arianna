package com.actors

import akka.actor.{Actor, ActorRef}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Put, Subscribe, SubscribeAck}
import ontologies.Topic

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
abstract class BasicSubscriber extends BasicActor {

    val topics: Set[Topic] // To Override Necessarily
    var ackTopicReceived: Integer = 0

    var mediator: ActorRef = _

    override protected def init(args: List[Any]): Unit = {
        super.init(args)
        mediator = DistributedPubSub(context.system).mediator
        mediator ! Put(self) // Point 2 Point Messaging with other Actors of the cluster
        topics.foreach(topic => mediator ! Subscribe(topic, self))
    }

    override protected def receptive = {
        case SubscribeAck(Subscribe(topic, None, this.self)) =>
            log.info("{} Successfully Subscribed to {}", name, topic)
            ackTopicReceived = ackTopicReceived + 1
            if (ackTopicReceived == topics.size) {
                this.context.become(subscribed, discardOld = true)
                log.info("I've become Subscribed!")
            }

        case msg => super.resistive(msg)
    }

    protected def subscribed: Actor.Receive = ???
}

/**
  * This class gives a common template for a Akka Publisher
  *
  */
abstract class BasicPublisher extends BasicActor {

    // activate the extension
    var mediator: ActorRef = _

    // Point 2 Point Messaging with other Actors of the cluster

    override protected def init(args: List[Any]): Unit = {
        super.init(args)
        mediator = DistributedPubSub(context.system).mediator
        mediator ! Put(self) // Point 2 Point Messaging with other Actors of the cluster
    }

}