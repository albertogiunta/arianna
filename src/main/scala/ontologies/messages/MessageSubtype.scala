package ontologies.messages

import ontologies.messages.MessageType._


/**
  * Created by Xander_C on 03/07/2017.
  */
trait MessageSubtype {
    
    val superType: MessageType
    val subtypeName: String
    
    def unmarshal(json: String): MessageContent
    
    def marshal(jso: MessageContent): String
    
    override def toString: String = superType.typeName + "/" + subtypeName
    
    override def equals(obj: Any) = obj match {
        case that: MessageSubtype => that.toString == this.toString
    }
}

object MessageSubtype {
    
    implicit def subtype2String(st: MessageSubtype): String = st.subtypeName
    
    implicit def string2Subtype(st: String): MessageSubtype = MessageSubtype.Factory(st)
    
    object Factory {
        def apply(subtypeName: String): MessageSubtype = subtypeName.toLowerCase match {
            case st if st == Init.Subtype.Greetings.toLowerCase =>
                Init.Subtype.Greetings
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
            case st if st == Update.Subtype.Position.toLowerCase =>
                Update.Subtype.Position
            case st if st == Update.Subtype.ActualLoad.toLowerCase =>
                Update.Subtype.ActualLoad
            case st if st == Update.Subtype.AdminUpdate.toLowerCase =>
                Update.Subtype.AdminUpdate
            case st if st == Update.Subtype.Practicability.toLowerCase =>
                Update.Subtype.Practicability
            case st if st == Update.Subtype.UserPosition.toLowerCase =>
                Update.Subtype.UserPosition
            case st if st == Update.Subtype.CellOccupation.toLowerCase =>
                Update.Subtype.CellOccupation
            case st if st == Update.Subtype.CellUpdate.toLowerCase =>
                Update.Subtype.CellUpdate
            case _ => null
        }
    }
    
}

object TestSubtypes extends App {
    
    println(MessageSubtype.Factory(MessageType.Update.Subtype.Sensors.subtypeName))
}