package ontologies

import ontologies.MessageType._

/**
  * Created by Xander_C on 03/07/2017.
  */
trait MessageSubtype {
    
    val subtypeName: String
    
    override def toString: String = subtypeName
    
    override def equals(obj: Any) = obj match {
        case that: MessageSubtype => that.toString == this.toString
    }
}

final case class AriadneMessageSubtype(override val subtypeName: String) extends MessageSubtype

object MessageSubtypeFactory {
    def apply(subtypeName: String): MessageSubtype = subtypeName.toLowerCase match {
        case st if st == Init.Subtype.Basic.toString.toLowerCase =>
            Init.Subtype.Basic
        case st if st == Alarm.Subtype.Basic.toString.toLowerCase =>
            Alarm.Subtype.Basic
        case st if st == Handshake.Subtype.Basic.toString.toLowerCase =>
            Handshake.Subtype.Basic
        case st if st == Route.Subtype.Basic.toString.toLowerCase =>
            Route.Subtype.Basic
        case st if st == Route.Subtype.Escape.toString.toLowerCase =>
            Route.Subtype.Escape
        case st if st == Topology.Subtype.Planimetrics.toString.toLowerCase =>
            Topology.Subtype.Planimetrics
        case st if st == Topology.Subtype.RealTopology.toString.toLowerCase =>
            Topology.Subtype.RealTopology
        case st if st == Topology.Subtype.Topology4Cell.toString.toLowerCase =>
            Topology.Subtype.Topology4Cell
        case st if st == Topology.Subtype.Topology4User.toString.toLowerCase =>
            Topology.Subtype.Topology4User
        case st if st == Update.Subtype.Sensors.toString.toLowerCase =>
            Update.Subtype.Sensors
        case st if st == Update.Subtype.Practicability.toString.toLowerCase =>
            Update.Subtype.Practicability
        case st if st == Update.Subtype.Position.toString.toLowerCase =>
            Update.Subtype.Position
        case st if st == Update.Subtype.CellOccupation.toString.toLowerCase =>
            Update.Subtype.CellOccupation
        case _ => null
    }
}
