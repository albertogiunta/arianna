package ontologies.messages

import ontologies.messages.MessageType._
import ontologies.test.MessageTypeForTest

/**
  * Created by Matteo Gabellini on 28/06/2017.
  */

trait MessageForTest[T] {
    
    def supertype: MessageTypeForTest
    
    def subtype: MessageSubtypeForTest
    
    def direction: MessageDirection
    
    def content: T
    
    override def toString =
        "Message of type(" + supertype.toString + "." + subtype.toString + ") " +
            direction.toString +
            " Message Content is \"" + content.toString + "\""
    
    override def equals(obj: Any) = obj match {
        case msg: MessageForTest[_] =>
            msg.supertype == this.supertype && msg.content == this.content
        case _ => false
    }
}

final case class AriadneLocalMessageForTest[T <: MessageContent](supertype: MessageTypeForTest,
                                                                 subtype: MessageSubtypeForTest,
                                                                 direction: MessageDirection,
                                                                 content: T) extends MessageForTest[T]

final case class AriadneRemoteMessageForTest(supertype: MessageTypeForTest,
                                             subtype: MessageSubtypeForTest,
                                             direction: MessageDirection,
                                             content: String) extends MessageForTest[String]

object TestMessageForTest extends App {
    val s: String = Init
    
    println(Init == "Init")
    
    println(MessageType.Factory("INIT"))
}