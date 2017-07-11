package ontologies

import ontologies.messages.Location._
import ontologies.messages.{AriadneMessage, Empty, Message, MessageType}
/**
  * Created by Alessandro on 28/06/2017.
  */
object TestOntologies extends App {
    val msg = AriadneMessage(
        MessageType.Alarm,
        MessageType.Alarm.Subtype.Basic,
        messages.Location.Self >> messages.Location.Self,
        Empty())
    
    val checker: Message[_] => Unit = {
        case msg@AriadneMessage(MessageType.Alarm, _, _, _) => println(msg.supertype.toString)
        case AriadneMessage(_, _, _, _) => println("Fuck-off")
        case _ => println("Ciao.")
    }

    checker(msg)
}
