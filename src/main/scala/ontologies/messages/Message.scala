package ontologies.messages

import ontologies.messages.MessageType._

/**
  * Created by Matteo Gabellini on 28/06/2017.
  */

trait Message[T] {
    
    def supertype: MessageType
    
    def subtype: MessageSubtype

    def direction: MessageDirection
    
    def content: T
    
    override def toString =
        "Message of type(" + supertype.toString + "." + subtype.toString + ") " +
            direction.toString +
            " Message Content is \"" + content.toString + "\""
    
    override def equals(obj: Any) = obj match {
        case msg: Message[_] =>
            msg.supertype == this.supertype && msg.content == this.content
        case _ => false
    }
}

final case class AriadneLocalMessage[T](supertype: MessageType,
                                        subtype: MessageSubtype,
                                        direction: MessageDirection,
                                        content: T) extends Message[T]

final case class AriadneRemoteMessage(supertype: MessageType,
                                      subtype: MessageSubtype,
                                      direction: MessageDirection,
                                      content: String) extends Message[String]

object TestMessage extends App {
    val s: String = Init
    
    println(Init == "Init")

    println(MessageType.Factory("INIT"))
}