package master.core

import akka.actor.ActorSelection
import common.BasicSubscriber
import ontologies.Topic
import ontologies.messages.MessageType.Alarm
import ontologies.messages.{AlarmContent, AriadneMessage, Message}

/**
  * Alarm Supervisor has to react only to Alarms of triggered in the system,
  * such that those messages are handled preemptively,
  * without being delayed by other messages in the MailBox
  *
  * Created by Xander_C on 11/07/2017.
  */
class AlarmSupervisor extends BasicSubscriber {
    override val topics = Set(Topic.Alarms)
    
    private var topologySupervisor: ActorSelection = _
    private var admin: ActorSelection = _
    
    override protected def init(args: List[Any]) = {
        log.info("Hello there from {}!", name)
        topologySupervisor = sibling("TopologySupervisor").get
        admin = sibling("AdminManager").get
    }
    
    override protected def receptive = {
        case msg@AriadneMessage(Alarm, _, _, _) => triggerAlarm(msg.asInstanceOf[Message[AlarmContent]])
    }
    
    private def triggerAlarm(msg: Message[AlarmContent]) = {
        log.info("Got {} from {}", msg.toString, sender.path.name)
        
        topologySupervisor forward msg
        admin forward msg
    }
}
