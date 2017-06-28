package ontologies

/**
  * Created by Alessandro on 28/06/2017.
  */

//object Message {
//
//    def apply(contentType : String, content: Any) : Message = MyMessage(contentType, content)
//
//    def unapply(msg: Message) : Option[(String, Any)] = Some(msg.contentType, msg.content)
//}

/**
  * Created by Alessandro on 28/06/2017.
  */
object TestOntologies extends App {
    val msg = MyMessage(Alarm, 666)
    
    val checker: MyMessage => Unit = {
        case msg@MyMessage(Alarm, _) => println(msg.messageType.typeName)
        case msg@MyMessage(_, _) => println("Fuck-off")
        case _ => println("Ciao.")
    }
    
    checker(msg)
}
