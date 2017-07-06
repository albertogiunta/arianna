package ontologies

import ontologies.messages.Location._
import ontologies.messages.{AriadneRemoteMessage, Message, MessageType}
/**
  * Created by Alessandro on 28/06/2017.
  */
object TestOntologies extends App {
    val msg = AriadneRemoteMessage(
        MessageType.Alarm,
        MessageType.Alarm.Subtype.Basic,
        messages.Location.Self >> messages.Location.Self,
        "666")
    
    val checker: Message[_] => Unit = {
        case msg@AriadneRemoteMessage(MessageType.Alarm, _, _, _) => println(msg.supertype.toString)
        case AriadneRemoteMessage(_, _, _, _) => println("Fuck-off")
        case _ => println("Ciao.")
    }

    checker(msg)
}
