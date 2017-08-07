package ontologies.messages

/**
  * Created by Matteo Gabellini on 28/06/2017.
  */

trait Message[T] {
    
    def supertype: MessageType
    
    def subtype: MessageSubtype
    
    def direction: MessageDirection
    
    def content: T
    
    override def toString =
        "Message of type(" + subtype.toString + ")|" + direction.toString + "|Message Content is \"" + content.toString + "\"\n"
    
    override def equals(obj: Any) = obj match {
        case msg: Message[_] =>
            msg.supertype == this.supertype && msg.content == this.content
        case _ => false
    }
}

final case class AriadneMessage[T <: MessageContent](supertype: MessageType,
                                                     subtype: MessageSubtype,
                                                     direction: MessageDirection,
                                                     content: T) extends Message[T]