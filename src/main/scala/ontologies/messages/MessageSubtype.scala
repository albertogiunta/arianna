package ontologies.messages

import ontologies.messages.MessageType._


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

//final case class AriadneMessageSubtype(override val subtypeName: String) extends MessageSubtype

object MessageSubtype {
    implicit def subtype2String(st: MessageSubtype): String = st.subtypeName

    object Factory {
        def apply(subtypeName: String): MessageSubtype = subtypeName.toLowerCase match {
            case st if st == Init.Subtype.Basic.toLowerCase =>
                Init.Subtype.Basic
            case st if st == Alarm.Subtype.Basic.toLowerCase =>
                Alarm.Subtype.Basic
            case st if st == Handshake.Subtype.Cell2Master.toLowerCase =>
                Handshake.Subtype.Cell2Master
            case st if st == Handshake.Subtype.Cell2User.toLowerCase =>
                Handshake.Subtype.Cell2User
            case st if st == Route.Subtype.Basic.toLowerCase =>
                Route.Subtype.Basic
            case st if st == Route.Subtype.Escape.toLowerCase =>
                Route.Subtype.Escape
            case st if st == Topology.Subtype.Planimetrics.toLowerCase =>
                Topology.Subtype.Planimetrics
            case st if st == Topology.Subtype.Topology4CellLight.toLowerCase =>
                Topology.Subtype.Topology4CellLight
            case st if st == Topology.Subtype.Topology4Cell.toLowerCase =>
                Topology.Subtype.Topology4Cell
            case st if st == Topology.Subtype.Topology4User.toLowerCase =>
                Topology.Subtype.Topology4User
            case st if st == Update.Subtype.Sensors.toLowerCase =>
                Update.Subtype.Sensors
            case st if st == Update.Subtype.Practicability.toLowerCase =>
                Update.Subtype.Practicability
            case st if st == Update.Subtype.Position.toLowerCase =>
                Update.Subtype.Position
            case st if st == Update.Subtype.ActualLoad.toLowerCase =>
                Update.Subtype.ActualLoad
            case st if st == Update.Subtype.AdminUpdate.toLowerCase =>
                Update.Subtype.AdminUpdate
            case _ => null
        }
    }

}
