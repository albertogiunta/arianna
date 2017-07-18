package master.core

import akka.actor.ActorSelection
import common.BasicSubscriber
import ontologies.Topic
import ontologies.messages.AriadneMessage
import ontologies.messages.MessageType.Alarm
import ontologies.messages.MessageType.Alarm.Subtype.{Basic, FromInterface}

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
    
        case msg@AriadneMessage(Alarm, Basic, _, _) =>
            log.info("Got {} from {}", msg.toString, sender.path.name)
            admin forward msg
    
        case msg@AriadneMessage(Alarm, FromInterface, _, _) =>
            log.info("Got {} from {}", msg.toString, sender.path.name)
    
        case _ => desist _
    }
}
