package master.core

import akka.actor.{ActorRef, ActorSelection}
import com.actors.BasicSubscriber
import ontologies.Topic
import ontologies.messages.AriadneMessage
import ontologies.messages.MessageType.Alarm
import ontologies.messages.MessageType.Alarm.Subtype.FromCell
import system.names.NamingSystem

/**
  * Alarm Supervisor has to react only to Alarms of triggered in the system,
  * such that those messages are handled preemptively,
  * without being delayed by other messages in the MailBox
  *
  * Created by Xander_C on 11/07/2017.
  */
class AlarmSupervisor(mediator: ActorRef) extends BasicSubscriber(mediator) {
    override val topics = Set(Topic.Alarms)
    
    private val topologySupervisor: () => ActorSelection = () => sibling(NamingSystem.TopologySupervisor).get
    private val admin: () => ActorSelection = () => sibling(NamingSystem.AdminManager).get
    
    override protected def subscribed = {
       
        case msg@AriadneMessage(Alarm, subtype, _, _) =>
            log.info("Got {} from {}", msg.toString, sender.path.name)
        
            subtype match {
                case FromCell => admin() forward msg
                case _ => // Ignore
            }
    
            topologySupervisor() forward msg
    
        case _ => desist _
    }
}
