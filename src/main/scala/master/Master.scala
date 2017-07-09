package master

import akka.actor.Props
import common.{ClusterMembersListener, CustomActor}
import master.cluster.{DataStreamer, MasterPublisher, MasterSubscriber, TopologySupervisor}

/**
  * Created by Alessandro on 29/06/2017.
  */
class Master extends CustomActor {
    
    override def preStart = {
        
        val listener = context.actorOf(Props[ClusterMembersListener], "ClusterListener")
        
        val subscriber = context.actorOf(Props[MasterSubscriber], "Subscriber")
        
        val publisher = context.actorOf(Props[MasterPublisher], "Publisher")
        
        val topologySupervisor = context.actorOf(Props[TopologySupervisor], "TopologySupervisor")
        
        val dataStreamer = context.actorOf(Props[DataStreamer], "DataStreamer")
    }
    
    override def receive: Receive = {
        case _ => log.info("Cazzo mi invii messaggi, stronzo!")
    }
}

