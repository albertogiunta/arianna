package ontologies.messages

import ontologies.test.MessageTypeForTest._


/**
  * Created by Xander_C on 03/07/2017.
  */
trait MessageSubtypeForTest {
    
    val subtypeName: String
    
    def unmarshal(json: String): MessageContent
    
    def marshal(jso: MessageContent): String
    
    override def toString: String = subtypeName
    
    override def equals(obj: Any) = obj match {
        case that: MessageSubtype => that.toString == this.toString
    }
}

//final case class AriadneMessageSubtype(override val subtypeName: String) extends MessageSubtype

object MessageSubtypeForTest {
    implicit def subtype2String(st: MessageSubtypeForTest): String = st.subtypeName
    
    object Factory {
        def apply(subtypeName: String): MessageSubtypeForTest = subtypeName.toLowerCase match {
            case st if st == InitForTest.Subtype.BasicForTest.toLowerCase =>
                InitForTest.Subtype.BasicForTest
            case st if st == AlarmForTest.Subtype.BasicForTest.toLowerCase =>
                AlarmForTest.Subtype.BasicForTest
            case st if st == HandshakeForTest.Subtype.Cell2MasterForTest.toLowerCase =>
                HandshakeForTest.Subtype.Cell2MasterForTest
            case st if st == HandshakeForTest.Subtype.Cell2UserForTest.toLowerCase =>
                HandshakeForTest.Subtype.Cell2UserForTest
            case st if st == RouteForTest.Subtype.BasicForTest.toLowerCase =>
                RouteForTest.Subtype.BasicForTest
            case st if st == RouteForTest.Subtype.EscapeForTest.toLowerCase =>
                RouteForTest.Subtype.EscapeForTest
            case st if st == TopologyForTest.Subtype.PlanimetricsForTest.toLowerCase =>
                TopologyForTest.Subtype.PlanimetricsForTest
            case st if st == TopologyForTest.Subtype.Topology4CellLightForTest.toLowerCase =>
                TopologyForTest.Subtype.Topology4CellLightForTest
            case st if st == TopologyForTest.Subtype.Topology4CellForTest.toLowerCase =>
                TopologyForTest.Subtype.Topology4CellForTest
            case st if st == TopologyForTest.Subtype.Topology4UserForTest.toLowerCase =>
                TopologyForTest.Subtype.Topology4UserForTest
            case st if st == UpdateForTest.Subtype.SensorsForTest.toLowerCase =>
                UpdateForTest.Subtype.SensorsForTest
            //            case st if st == UpdateForTest.Subtype.PracticabilityForTest.toLowerCase =>
            //                UpdateForTest.Subtype.PracticabilityForTest
            case st if st == UpdateForTest.Subtype.PositionForTest.toLowerCase =>
                UpdateForTest.Subtype.PositionForTest
            case st if st == UpdateForTest.Subtype.ActualLoadForTest.toLowerCase =>
                UpdateForTest.Subtype.ActualLoadForTest
            case st if st == UpdateForTest.Subtype.AdminUpdateForTest.toLowerCase =>
                UpdateForTest.Subtype.AdminUpdateForTest
            case _ => null
        }
    }
    
}
