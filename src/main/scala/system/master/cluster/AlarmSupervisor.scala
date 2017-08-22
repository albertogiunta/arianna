package system.master.cluster

import akka.actor.{ActorRef, ActorSelection}
import com.actors.TemplateSubscriber
import system.names.NamingSystem
import system.ontologies.Topic
import system.ontologies.messages.AriadneMessage
import system.ontologies.messages.MessageType.Alarm
import system.ontologies.messages.MessageType.Alarm.Subtype.FromCell

/**
  * Alarm Supervisor has to react only to Alarms of triggered in the system,
  * such that those messages are handled preemptively,
  * without being delayed by other messages in the MailBox
  *
  * Created by Xander_C on 11/07/2017.
  */
class AlarmSupervisor(mediator: ActorRef) extends TemplateSubscriber(mediator) {
    override val topics = Set(Topic.Alarms)
    
    private val topologySupervisor: () => ActorSelection = () => sibling(NamingSystem.TopologySupervisor).get
    private val admin: () => ActorSelection = () => sibling(NamingSystem.AdminSupervisor).get
    
    override protected def subscribed: Receive = {
       
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
