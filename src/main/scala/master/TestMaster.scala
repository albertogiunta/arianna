package master

import akka.actor.{ActorSystem, Props}
import master.cluster.Subscriber

object TestMaster extends App {
    
    val system = ActorSystem("Arianna-Master-cluster")
    
    val actor = system.actorOf(Props[Subscriber], "Subscriber")
}
