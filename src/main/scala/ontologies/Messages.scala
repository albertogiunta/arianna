package ontologies

import ontologies.MessageType._

/**
  * Created by Matteo Gabellini on 28/06/2017.
  */

trait Message[T] {
    
    def supertype: MessageType
    
    def subtype: MessageSubtype
    
    def content: T
    
    override def toString =
        "Message of type(" + supertype.toString + "." + subtype.toString + ") " +
            "and Content was " + content.toString
    
    override def equals(obj: Any) = obj match {
        case msg: Message[_] =>
            msg.supertype == this.supertype && msg.content == this.content
        case _ => false
    }
}

final case class AriadneLocalMessage[T](supertype: MessageType,
                                        subtype: MessageSubtype,
                                        content: T) extends Message[T]

final case class AriadneRemoteMessage(supertype: MessageType,
                                      subtype: MessageSubtype,
                                      content: String) extends Message[String]

object TestMessage extends App {
    val s: String = Init
    
    println(Init == "Init")
    
    println(MessageTypeFactory("INIT"))
}