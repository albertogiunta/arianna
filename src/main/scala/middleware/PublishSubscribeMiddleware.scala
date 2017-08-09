package middleware

import akka.cluster.Cluster
import akka.cluster.ClusterEvent.{MemberRemoved, MemberUp}
import com.actors.CustomActor
import middleware.PublishSubscribeMiddleware.Message._

import scala.collection.mutable

class PublishSubscribeMiddleware extends CustomActor {
    
    implicit val cluster = Cluster(context.system)
    
    private val subscriptions: mutable.Map[String, mutable.HashSet[String]] = mutable.HashMap.empty
    
    private val seeds: mutable.HashSet[String] = mutable.HashSet(cluster.remotePathOf(self).address.toString)
    
    private val peers: mutable.HashSet[String] = mutable.HashSet.empty
    
    override def preStart(): Unit = {
        super.preStart()
        
        cluster.subscribe(self, classOf[MemberUp], classOf[MemberRemoved])
    }
    
    override def receive: Receive = {
    
        case MemberUp(seed) if seed.address != self.path.address =>
            if (seeds.add(seed.address.toString)) {
            
                subscriptions.keysIterator.foreach(topic =>
                    subscriptions(topic).foreach(subscriber =>
                        context.actorSelection(seed.address + "/user/" + name) ! Subscribe(topic, subscriber)
                    )
                )
            
                peers.foreach(peer =>
                    context.actorSelection(seed.address + "/user/" + name) ! Put(peer)
                )
            }
    
        case MemberRemoved(seed, _) =>
            seeds.remove(seed.address.toString)
        
        case msg@Subscribe(topic, actor) =>
    
            var forwardToSeeds = false
            
            if (subscriptions.get(topic).nonEmpty) {
                forwardToSeeds = subscriptions(topic).add(actor)
            } else {
                subscriptions.put(topic, mutable.HashSet(actor))
                forwardToSeeds = true
            }
    
            if (sender.path.address == self.path.address) {
        
                context.actorSelection(actor) ! SubscribeAck(msg)
        
                if (forwardToSeeds) {
                    seeds.filter(p => p != cluster.remotePathOf(self).address.toString)
                        .foreach(p => context.actorSelection(p + "/user/" + name) forward
                            msg.copy(subscriber = cluster.remotePathOf(sender).toString))
                }
            }
    
        case Publish(topic, msg) if sender != self =>
            subscriptions(topic).foreach(a => context.actorSelection(a) forward msg)
    
        case msg@Put(actor) =>
        
            peers.add(actor)
        
            seeds.foreach(seed => context.actorSelection(seed + "/user/" + name) ! msg)
    
        case send@Send(target, msg) =>
        
            if (peers(target)) {
                context.actorSelection(target) forward msg
                sender ! SendAck(send)
            }
            
        case _ =>
    }
}

object PublishSubscribeMiddleware {
    
    final val name = "PubSubMiddleware"
    
    object Message {
        
        final case class Hello(from: String) extends Serializable
        
        final case class Subscribe(topic: String, subscriber: String) extends Serializable
    
        final case class SubscribeAck(subscrition: Subscribe) extends Serializable
        
        final case class Publish(topic: String, msg: Any) extends Serializable
    
        final case class PublishAck(publishing: Publish) extends Serializable
        
        final case class Send(target: String, msg: Any) extends Serializable
    
        final case class SendAck(p2p: Send) extends Serializable
    
        final case class Put(self: String) extends Serializable
    }
    
}