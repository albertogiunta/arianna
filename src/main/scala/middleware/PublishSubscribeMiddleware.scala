package middleware

import akka.cluster.Cluster
import akka.cluster.ClusterEvent.{MemberRemoved, MemberUp}
import com.actors.CustomActor
import middleware.PublishSubscribeMiddleware.Message._

import scala.collection.mutable

class PublishSubscribeMiddleware extends CustomActor {
    
    implicit val cluster = Cluster(context.system)
    
    private val subscriptions: mutable.Map[String, mutable.HashSet[String]] = mutable.HashMap.empty
    
    private val peers: mutable.HashSet[String] = mutable.HashSet(cluster.remotePathOf(self).address.toString)
    
    override def preStart(): Unit = {
        super.preStart()
        
        cluster.subscribe(self, classOf[MemberUp], classOf[MemberRemoved])
    }
    
    override def receive: Receive = {
        
        case MemberUp(peer) if peer.address != self.path.address =>
            log.info("Found {} joining the cluster...", peer.address.toString)
            log.info("Adding {} to Peers...", peer.address.toString)
            peers.add(peer.address.toString)
            
            subscriptions.keysIterator.foreach(topic =>
                subscriptions(topic).foreach(subscriber =>
                    context.actorSelection(peer.address + "/user/" + name) ! Subscribe(topic, subscriber)
                )
            )
        
        case MemberRemoved(peer, _) =>
            log.warning("The peer {} has left the group...", peer.address.toString)
            log.info("Removing {} from Peers...", peer.address.toString)
            peers.remove(peer.address.toString)
        
        case msg@Subscribe(topic, actor) =>
            
            log.info("Adding subscription for topic {} from actor {}", topic, actor)
            
            if (subscriptions.get(topic).nonEmpty) {
                subscriptions(topic).add(actor)
            } else {
                subscriptions.put(topic, mutable.HashSet(actor))
            }
            
            //            log.info("Actual subscriptions are {}", subscriptions.toString)
            
            if (sender.path.address == self.path.address) {
                log.info("Sending Subscription ACK({}) to {}", topic, actor)
                
                context.actorSelection(actor) ! SubAck(topic)
                
                peers.filter(p => p != cluster.remotePathOf(self).address.toString)
                    .foreach(p => context.actorSelection(p + "/user/" + name) forward
                        msg.copy(subscriber = cluster.remotePathOf(sender).toString))
            }
        
        case Publish(topic, msg) if sender != self =>
            log.info("Publishing {} on topic {}...", msg.toString, topic)
            subscriptions(topic).foreach(a => context.actorSelection(a) forward msg)
        
        case Send(actor, msg) =>
            context.actorSelection(actor) forward msg
        case _ =>
    }
}

object PublishSubscribeMiddleware {
    
    final val name = "PubSubMiddleware"
    
    object Message {
        
        final case class Hello(from: String) extends Serializable
        
        final case class Subscribe(topic: String, subscriber: String) extends Serializable
        
        final case class SubAck(topic: String) extends Serializable
        
        final case class Publish(topic: String, msg: Any) extends Serializable
        
        final case class PubAck(topic: String) extends Serializable
        
        final case class Send(target: String, msg: Any) extends Serializable
        
        final case class SendAck(target: String, msg: Any) extends Serializable
        
    }
    
}