package ontologies.messages

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
    
    //implicit def direction2Message(d: Direction): MessageDirection = MessageDirectionImpl(d)

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
    
    def reverse: Direction = Direction(from = this.to, to = this.from)
}

object Location {
    
    final case class LocationImpl(loc: String) extends Location

    val Admin: Location = LocationImpl("Admin")
    
    val Master: Location = LocationImpl("Master")

    val Cell: Location = LocationImpl("Cell")

    val User: Location = LocationImpl("User")

    val Notifier: Location = LocationImpl("Notifier")

    val Self: Location = LocationImpl("Self")

    val Switcher: Location = LocationImpl("Switcher")

    val MovGenerator: Location = LocationImpl("MovGenerator")
    
    val PowerSupply: Location = LocationImpl("PowerSupplyGenerator")

    val Movement: Location = LocationImpl("Movement")

    val MovementGenerator: Location = LocationImpl("MovementGenerator")

    implicit def location2String(d: Location): String = d.toString
    
    implicit def location2Direction(d: Location => Direction, l: Location): Direction = d(l)
    
    implicit def direction2String(d: Direction): String = d.toString
    
    implicit def direction2Message(d: Direction): MessageDirection = MessageDirectionImpl(d)

    object Factory {
        def apply(s: String): Location = s.toLowerCase match {
            case loc if loc == Admin.toLowerCase => Admin
            case loc if loc == Master.toLowerCase => Master
            case loc if loc == Cell.toLowerCase => Cell
            case loc if loc == User.toLowerCase => User
            case loc if loc == Notifier.toLowerCase => Notifier
            case loc if loc == Self.toLowerCase => Self
            case loc if loc == Switcher.toLowerCase => Switcher
            case loc if loc == MovementGenerator.toLowerCase => MovementGenerator
            case _ => null
        }
    }

}

object TestMessageDirection extends App {
    
    println(Location.Master >> Location.Cell)
    
    //val msgDir: MessageDirection = Location.Server << Location.Admin
    
    //println(msgDir)

    println(Location.Factory("ADMIN"))
}