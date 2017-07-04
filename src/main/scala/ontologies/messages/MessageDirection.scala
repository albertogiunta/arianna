package ontologies.messages

import ontologies.messages.Location._

/**
  * Created by Alessandro on 04/07/2017.
  */

/**
  * A Message "Header" that specifies the Direction from which the packet is coming
  * and to which the packet is going.
  *
  */
trait MessageDirection {
    
    val iter: Direction
    
    override def toString = "Message is coming " + iter.toString
    
    override def equals(obj: scala.Any) = obj match {
        case that: MessageDirection => that.iter == this.iter
        case _ => false
    }
}

final case class MessageDirectionImpl(iter: Direction) extends MessageDirection

object MessageDirection {
    
    implicit def messageDirection2String(d: MessageDirection): String = d.iter.toString
    
    implicit def string2Message(d: Direction): MessageDirection = MessageDirectionImpl(d)
    
    implicit def message2Direction(md: MessageDirection): Direction = md.iter
}

/**
  * This trait defines a Builder of Direction with the use of the Companion object.
  *
  * Given another Location it creates a Direction from this location to the given location
  *
  */
trait Location {
    
    val loc: String
    
    def >>(that: Location): Direction = Direction(from = this.loc, to = that.loc)
    
    def <<(that: Location): Direction = Direction(from = that.loc, to = this.loc)
    
    override def toString = loc
    
    override def equals(obj: scala.Any) = obj match {
        case that: Location => that.loc == this.loc
    }
}

final case class Direction(from: String, to: String) {
    override def toString = "from " + from + " to " + to
}

object Location {
    
    private final case class DirectionImpl(loc: String) extends Location
    
    val Admin: Location = DirectionImpl("Admin")
    
    val Server: Location = DirectionImpl("Server")
    
    val Cell: Location = DirectionImpl("Cell")
    
    val User: Location = DirectionImpl("User")
    
    val Notifier: Location = DirectionImpl("Notifier")
    
    val Self: Location = DirectionImpl("Self")
    
    val Switcher: Location = DirectionImpl("Switcher")
    
    val MovGenerator: Location = DirectionImpl("MovGenerator")
    
    implicit def location2String(d: Location): String = d.toString
    
    implicit def direction2String(d: Direction): String = d.toString
    
    implicit def string2MessageDirection(d: Direction): MessageDirection = MessageDirectionImpl(d)
    
    object Factory {
        def apply(s: String): Location = s.toLowerCase match {
            case loc if loc == Admin.toLowerCase => Admin
            case loc if loc == Server.toLowerCase => Server
            case loc if loc == Cell.toLowerCase => Cell
            case loc if loc == User.toLowerCase => User
            case loc if loc == Notifier.toLowerCase => Notifier
            case loc if loc == Self.toLowerCase => Self
            case loc if loc == Switcher.toLowerCase => Switcher
            case loc if loc == MovGenerator.toLowerCase => MovGenerator
            case _ => null
        }
    }
    
}

object TestMessageDirection extends App {
    
    println(Admin >> Cell)
    
    val msgDir: MessageDirection = Server << Admin
    
    println(msgDir)
}