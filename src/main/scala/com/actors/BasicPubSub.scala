package com.actors

import akka.actor.{Actor, ActorRef}
import akka.cluster.Cluster
import akka.cluster.pubsub.DistributedPubSubMediator._
import ontologies.Topic
import ontologies.messages.Location._
import ontologies.messages.MessageType.Init
import ontologies.messages.{AriadneMessage, Greetings, Location, MessageContent}

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
abstract class TemplateSubscriber(mediator: ActorRef) extends TemplateActor {
    
    val cluster: Cluster = akka.cluster.Cluster(context.system)
    
    protected val topics: Set[Topic[MessageContent]] // To Override Necessarily
    private var ackTopicReceived: Int = 0

    override protected def init(args: List[Any]): Unit = {
        super.init(args)
        mediator ! Put(self) // Point 2 Point Messaging with other Actors of the cluster
        topics.foreach(topic => mediator ! Subscribe(topic, self))
    }
    
    override protected def receptive: Receive = {
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
    
    protected def subscribed: Actor.Receive
}

/**
  * This class gives a common template for a Akka Publisher
  *
  */
abstract class TemplatePublisher(mediator: ActorRef) extends TemplateActor {
    
    val cluster: Cluster = akka.cluster.Cluster(context.system)

    // Point 2 Point Messaging with other Actors of the cluster
    override protected def init(args: List[Any]): Unit = {
        super.init(args)
        mediator ! Put(self) // Point 2 Point Messaging with other Actors of the cluster
    }

}