package cell.cluster

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Publish
import ontologies.{MyMessage, ServerTopic}

/**
  * Actor that manages the sending of messages to the main server
  * Created by Matteo Gabellini on 29/06/2017.
  */
class CellPublisher extends Actor with ActorLogging {

  private val mediator: ActorRef = DistributedPubSub(context.system).mediator


  def receive = {

    case msg: MyMessage =>
      val topicName = ServerTopic.topicName
      mediator ! Publish(topicName, MyMessage(ontologies.Alarm, msg))
    case _ => // Ignore
  }
}