package ontologies.messages

/**
  * This trait represent a generic message with various header information,
  * like Type G and Subtype L of the message and the direction.
  *
  * In the end there is a Content of type C.
  *
  * Created by Matteo Gabellini and Alessandro Cevoli on 28/06/2017.
  */
trait Message[G, L, C] {
    
    def supertype: G
    
    def subtype: L
    
    def direction: MessageDirection
    
    def content: C
    
    override def toString: String =
        "Message of type(" + subtype.toString + ")|" + direction.toString + "|Message Content is \"" + content.toString + "\"\n"
    
    override def equals(obj: Any): Boolean = obj match {
        case msg: Message[_, _, _] =>
            msg.supertype == this.supertype && msg.subtype == this.subtype && msg.content == this.content
        case _ => false
    }
}

/**
  * This case class is an actual implementation of the trait Message
  *
  * @param supertype The Supertype Associated with this message
  * @param subtype   The Subtype Associated with this Message
  * @param direction The Direction of this message
  * @param content   The content of this message
  * @tparam C The Type of the content
  */
final case class AriadneMessage[C <: MessageContent](supertype: MessageType,
                                                     subtype: MessageSubtype,
                                                     direction: MessageDirection,
                                                     content: C) extends Message[MessageType, MessageSubtype, C]