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
    
    override def equals(obj: Any): Boolean = obj match {
        case that: MessageSubtype => that.toString == this.toString
    }
}

object MessageSubtype {
    
    implicit def subtype2String(st: MessageSubtype): String = st.toString
    
    object Factory {
        def apply(subTypeAsString: String): MessageSubtype = subTypeAsString.toLowerCase match {
        
            /* Init Messages  */
            case st if st == Init.Subtype.Greetings.toLowerCase =>
                Init.Subtype.Greetings
            case st if st == Init.Subtype.Goodbyes.toLowerCase =>
                Init.Subtype.Goodbyes

            /* Error Messages */
            case st if st == Error.Subtype.Generic.toLowerCase =>
                Error.Subtype.Generic
            case st if st == Error.Subtype.LookingForAMap.toLowerCase =>
                Error.Subtype.LookingForAMap
            case st if st == Error.Subtype.MapIdentifierMismatch.toLowerCase =>
                Error.Subtype.MapIdentifierMismatch
            case st if st == Error.Subtype.CellMappingMismatch.toLowerCase =>
                Error.Subtype.CellMappingMismatch
            /* Alarm Messages */
            case st if st == Alarm.Subtype.FromCell.toLowerCase =>
                Alarm.Subtype.FromCell
            case st if st == Alarm.Subtype.FromInterface.toLowerCase =>
                Alarm.Subtype.FromInterface

            /* Handshake Messages */
            case st if st == Handshake.Subtype.CellToMaster.toLowerCase =>
                Handshake.Subtype.CellToMaster
            case st if st == Handshake.Subtype.CellToUser.toLowerCase =>
                Handshake.Subtype.CellToUser
            case st if st == Handshake.Subtype.UserToCell.toLowerCase =>
                Handshake.Subtype.UserToCell
            case st if st == Handshake.Subtype.Acknowledgement.toLowerCase =>
                Handshake.Subtype.Acknowledgement

            /* Route Messages */
            case st if st == Route.Subtype.Request.toLowerCase =>
                Route.Subtype.Request
            case st if st == Route.Subtype.Response.toLowerCase =>
                Route.Subtype.Response
            case st if st == Route.Subtype.Info.toLowerCase =>
                Route.Subtype.Info

            /* Topology Messages */
            case st if st == Topology.Subtype.Planimetrics.toLowerCase =>
                Topology.Subtype.Planimetrics
            case st if st == Topology.Subtype.Practicabilities.toLowerCase =>
                Topology.Subtype.Practicabilities
            case st if st == Topology.Subtype.ViewedFromACell.toLowerCase =>
                Topology.Subtype.ViewedFromACell
            case st if st == Topology.Subtype.Topology4User.toLowerCase =>
                Topology.Subtype.Topology4User
            case st if st == Topology.Subtype.Acknowledgement.toLowerCase =>
                Topology.Subtype.Acknowledgement

            /* Update Messages */
            case st if st == Update.Subtype.Admin.toLowerCase =>
                Update.Subtype.Admin
            case st if st == Update.Subtype.Sensors.toLowerCase =>
                Update.Subtype.Sensors
            case st if st == Update.Subtype.CurrentPeople.toLowerCase =>
                Update.Subtype.CurrentPeople
            case st if st == Update.Subtype.Practicability.toLowerCase =>
                Update.Subtype.Practicability
            case st if st == Update.Subtype.Position.UserPosition.toLowerCase =>
                Update.Subtype.Position.UserPosition
            case st if st == Update.Subtype.Position.AntennaPosition.toLowerCase =>
                Update.Subtype.Position.AntennaPosition
            case st if st == Update.Subtype.CurrentPeople.toLowerCase =>
                Update.Subtype.CurrentPeople

            /* Info Messages */
            case st if st == Info.Subtype.Request.toLowerCase =>
                Info.Subtype.Request
            case st if st == Info.Subtype.Response.toLowerCase =>
                Info.Subtype.Response

            /* Interface Messages */
            case st if st == Interface.Subtype.OpenChart.toLowerCase =>
                Interface.Subtype.OpenChart
            case st if st == Interface.Subtype.UpdateChart.toLowerCase =>
                Interface.Subtype.UpdateChart
            case st if st == Interface.Subtype.CloseChart.toLowerCase =>
                Interface.Subtype.CloseChart

            case _ => null
        }
    }
    
}