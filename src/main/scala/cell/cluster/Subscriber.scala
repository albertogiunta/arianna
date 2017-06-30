package cell.cluster

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.cluster.pubsub.DistributedPubSubMediator.{Put, Send}
import akka.cluster.pubsub._

/**
  * Created by Alessandro on 28/06/2017.
  */
class Subscriber extends Actor with ActorLogging {
    
    import DistributedPubSubMediator.{Subscribe, SubscribeAck}
    
    val mediator: ActorRef = DistributedPubSub(context.system).mediator
    
    // subscribe to the topic named "content"
    mediator ! Subscribe("content", self)
    mediator ! Put(self)
    
    def receive: PartialFunction[Any, Unit] = {
        case msg: String =>
            log.info("Got {}", msg)

        case msg@SubscribeAck(Subscribe("content", None, `self`)) =>
            log.info("subscribing")

        case s: String =>
            log.info("Got {}", s)

        case in: String ⇒
            val out = in.toUpperCase
            mediator ! Send(path = "/user/destination", msg = out, localAffinity = true)
    }
}