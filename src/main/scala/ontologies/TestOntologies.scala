package ontologies

/**
  * Created by Alessandro on 28/06/2017.
  */
object TestOntologies extends App {
    val msg = AriadneRemoteMessage(MessageType.Alarm, null, "666")
    
    val checker: Message[_] => Unit = {
        case msg@AriadneRemoteMessage(MessageType.Alarm, _, _) => println(msg.supertype.toString)
        case AriadneRemoteMessage(_, _, _) => println("Fuck-off")
        case _ => println("Ciao.")
    }

    checker(msg)
}
