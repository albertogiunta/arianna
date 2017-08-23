package system.ontologies.messages

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
    
    override def toString: String = "Message is coming " + iter.toString
    
    override def equals(obj: scala.Any): Boolean = obj match {
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
    
    override def toString: String = loc
    
    override def equals(obj: scala.Any): Boolean = obj match {
        case that: Location => that.loc == this.loc
    }
}

final case class Direction(from: String, to: String) {
    override def toString: String = "from " + from + " to " + to
    
    def reverse: Direction = Direction(from = this.to, to = this.from)
}

object Location {
    
    final case class LocationImpl(loc: String) extends Location

    val Admin: Location = LocationImpl("Admin")
    
    val Master: Location = LocationImpl("Master")

    val Cell: Location = LocationImpl("Cell")

    val Cluster: Location = LocationImpl("Cluster")

    val User: Location = LocationImpl("User")

    val Notifier: Location = LocationImpl("Notifier")

    val Self: Location = LocationImpl("Self")

    implicit def location2String(d: Location): String = d.toString
    
    implicit def location2Direction(d: Location => Direction, l: Location): Direction = d(l)
    
    implicit def direction2String(d: Direction): String = d.toString
    
    implicit def direction2Message(d: Direction): MessageDirection = MessageDirectionImpl(d)
    
    object StaticFactory {
        def apply(s: String): Location = s.toLowerCase match {
            case loc if loc == Admin.toLowerCase => Admin
            case loc if loc == Master.toLowerCase => Master
            case loc if loc == Cell.toLowerCase => Cell
            case loc if loc == User.toLowerCase => User
            case loc if loc == Notifier.toLowerCase => Notifier
            case loc if loc == Self.toLowerCase => Self
            case loc if loc == Cluster.toLowerCase => Cluster
            case _ => null
        }
    }
    
    object PreMade {
        val selfToSelf: MessageDirection = Location.Self >> Location.Self
        val cellToMaster: MessageDirection = Location.Cell >> Location.Master
        val masterToCell: MessageDirection = cellToMaster.reverse
        val adminToMaster: MessageDirection = Location.Admin >> Location.Master
        val masterToAdmin: MessageDirection = adminToMaster.reverse
        val masterToCluster: MessageDirection = Location.Master >> Location.Cluster
        val cellToCluster: MessageDirection = Location.Cell >> Location.Cluster
        val cellToUser: MessageDirection = Location.Cell >> Location.User
        val userToCell: MessageDirection = cellToUser.reverse
        val cellToCell: MessageDirection = Location.Cell >> Location.Cell
    }

}

object TryMessageDirection extends App {
    
    println(Location.Master >> Location.Cell)

    //val msgDir: MessageDirection = Location.Master << Location.Admin
    
    //println(msgDir)
    
    println(Location.StaticFactory("ADMIN"))
}