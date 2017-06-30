package ontologies

/**
  * Created by Alessandro on 28/06/2017.
  */
object TestOntologies extends App {
    val msg = AriadneMessage(Alarm, "666")
    
    val checker: Message => Unit = {
        case msg@AriadneMessage(Alarm, _) => println(msg.messageType.typeName)
        case msg@AriadneMessage(_, _) => println("Fuck-off")
        case _ => println("Ciao.")
    }
    
    checker(msg)
}
