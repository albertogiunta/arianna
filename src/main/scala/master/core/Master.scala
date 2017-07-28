package master.core

import akka.actor.Props
import com.actors.{ClusterMembersListener, CustomActor}
import master.cluster._

/**
  * Created by Alessandro on 29/06/2017.
  */
class Master extends CustomActor {
    
    override def preStart = {
    
        val adminManager = context.actorOf(Props[AdminManager], "AdminManager")
        
        val listener = context.actorOf(Props[ClusterMembersListener], "ClusterListener")
        
        val subscriber = context.actorOf(Props[MasterSubscriber], "Subscriber")
        
        val publisher = context.actorOf(Props[MasterPublisher], "Publisher")
        
        val topologySupervisor = context.actorOf(Props[TopologySupervisor], "TopologySupervisor")
    
        val dataStreamer = context.actorOf(Props(new DataStreamer()), "DataStreamer")
    
        val alarmSupervisor = context.actorOf(Props[AlarmSupervisor], "AlarmSupervisor")

    }
    
    override def receive: Receive = {
        case _ => log.info("Cazzo mi invii messaggi, stronzo!")
    }
}

