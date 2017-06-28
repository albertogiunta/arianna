package master.cluster

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Publish, Put, Send}

/**
  * Created by Alessandro on 28/06/2017.
  */
class Publisher extends Actor with ActorLogging {
    
    // activate the extension
    val mediator: ActorRef = DistributedPubSub(context.system).mediator
    mediator ! Put(self)
    
    def receive: PartialFunction[Any, Unit] = {
    
        case in: String =>
            val out = in.toUpperCase
            mediator ! Publish("content", out)
    
        case s: String =>
            log.info("Got {}", s)
    
        case in: String ⇒
            val out = in.toUpperCase
            mediator ! Send(path = "/user/destination", msg = out, localAffinity = true)
    }
}
