package master

import akka.actor.{ActorSystem, Props}
import master.cluster.Subscriber

object TestMaster extends App {
    
    val system = ActorSystem("Arianna-Master-cluster")
    //ERROR] [06/28/2017 19:49:07.571] [Arianna-Master-cluster-akka.actor.default-dispatcher-2]
    // [akka://Arianna-Master-cluster/user/Subscriber] ActorSystem [akka://Arianna-Master-cluster]
    // needs to have a 'ClusterActorRefProvider' enabled in the configuration,
    // currently uses [akka.actor.LocalActorRefProvider]
    val actor = system.actorOf(Props[Subscriber], "Subscriber")
}
