package ontologies.test

import ontologies.messages.Location._
import ontologies.messages.{Location, _}
import ontologies.test.MessageTypeForTest._
import spray.json.{DefaultJsonProtocol, _}

/**
  * Created by Xander_C on 03/07/2017.
  */

object AriannaJsonProtocolForTest extends DefaultJsonProtocol {
    implicit val pointFormat: RootJsonFormat[Point] = jsonFormat2(Point)
    implicit val coordinatesFormat: RootJsonFormat[Coordinates] = jsonFormat4(Coordinates)
    implicit val infoCellFormat: RootJsonFormat[InfoCell] = jsonFormat5(InfoCell)
    implicit val passageFormat: RootJsonFormat[Passage] = jsonFormat3(Passage)
    implicit val sensorFormat: RootJsonFormat[Sensor] = jsonFormat2(Sensor)
    implicit val sensorListFormat: RootJsonFormat[SensorList] = jsonFormat2(SensorList.apply)
    implicit val cellFormat: RootJsonFormat[Cell] = jsonFormat10(ontologies.messages.Cell)
    implicit val areaFormat: RootJsonFormat[Area] = jsonFormat2(Area)
    implicit val cellForUserFormat: RootJsonFormat[CellForUser] = jsonFormat4(CellForUser.apply)
    implicit val cellForCellFormat: RootJsonFormat[CellForCell] = jsonFormat6(CellForCell.apply)
    implicit val areaForCellFormat: RootJsonFormat[AreaForCell] = jsonFormat2(AreaForCell.apply)
    implicit val cellUpdateFormat: RootJsonFormat[CellUpdate] = jsonFormat3(CellUpdate.apply)
    implicit val updateForAdminFormat: RootJsonFormat[UpdateForAdmin] = jsonFormat1(UpdateForAdmin)
    implicit val actualLoadUpdateFormat: RootJsonFormat[ActualLoadUpdate] = jsonFormat2(ActualLoadUpdate.apply)
    implicit val alarmContentFormat: RootJsonFormat[AlarmContent] = jsonFormat3(AlarmContent.apply)
    implicit val greetingsFormat: RootJsonFormat[Greetings] = jsonFormat1(Greetings)
    implicit val lightCellFormat: RootJsonFormat[LightCell] = jsonFormat3(LightCell.apply)
    implicit val lightAreaFormat: RootJsonFormat[LightArea] = jsonFormat2(LightArea.apply)
}

import ontologies.messages.AriannaJsonProtocol._

trait MessageTypeForTest {
    
    val typeName: String
    
    override def toString: String = typeName
    
    override def equals(obj: scala.Any) = obj match {
        case o: MessageType => o.toString == this.toString
        case o: String => o == this.toString
    }
}

object MessageTypeForTest {
    
    object InitForTest extends MessageTypeForTest {
        
        override val typeName = "Init"
        
        object Subtype {
            
            object BasicForTest extends MessageSubtypeForTest { // To be renamed Greetings
                
                override val subtypeName = "Init" // Greetings
                
                override def unmarshal(json: String): Greetings = null
                
                override def marshal(obj: MessageContent): String = null
            }
            
        }
        
    }
    
    object AlarmForTest extends MessageTypeForTest {
        
        override val typeName = "Alarm"
        
        object Subtype {
            
            object BasicForTest extends MessageSubtypeForTest {
                
                override val subtypeName = "Alarm"
                
                override def unmarshal(json: String): AlarmContent =
                    json.parseJson.convertTo[AlarmContent]
                
                override def marshal(obj: MessageContent): String =
                    obj.asInstanceOf[AlarmContent].toJson.toString()
            }
            
        }
        
    }
    
    object HandshakeForTest extends MessageTypeForTest {
        
        override val typeName = "Handshake"
        
        object Subtype {
            
            object BasicForTest extends MessageSubtypeForTest {
                
                override val subtypeName = "Handshake"
                
                override def unmarshal(json: String): MessageContent = null
                
                override def marshal(obj: MessageContent): String = null
            }
            
            object Cell2UserForTest extends MessageSubtypeForTest {
                
                override val subtypeName = "Cell2User"
                
                override def unmarshal(json: String): CellForUser = json.parseJson.convertTo[CellForUser]
                
                override def marshal(obj: MessageContent): String = obj.asInstanceOf[CellForUser].toJson.toString()
            }
            
            object Cell2MasterForTest extends MessageSubtypeForTest {
                
                override val subtypeName = "Cell2Master"
                
                override def unmarshal(json: String): InfoCell = json.parseJson.convertTo[InfoCell]
                
                override def marshal(obj: MessageContent): String = obj.asInstanceOf[InfoCell].toJson.toString()
            }
            
        }
        
    }
    
    object RouteForTest extends MessageTypeForTest {
        
        override val typeName = "Route"
        
        object Subtype {
            
            object BasicForTest extends MessageSubtypeForTest {
                
                override val subtypeName = "SimpleRoute"
                
                override def unmarshal(json: String): MessageContent = null
                
                override def marshal(obj: MessageContent): String = null
            }
            
            object EscapeForTest extends MessageSubtypeForTest {
                
                override val subtypeName = "EscapeRoute"
                
                override def unmarshal(json: String): MessageContent = null
                
                override def marshal(obj: MessageContent): String = null
            }
            
        }
        
    }
    
    object TopologyForTest extends MessageTypeForTest {
        
        override val typeName = "Topology"
        
        object Subtype {
            
            object PlanimetricsForTest extends MessageSubtypeForTest {
                
                override val subtypeName = "Planimetrics"
                
                override def unmarshal(json: String): Area = json.parseJson.convertTo[Area]
                
                override def marshal(obj: MessageContent): String = obj.asInstanceOf[InfoCell].toJson.toString()
            }
            
            
            object Topology4CellLightForTest extends MessageSubtypeForTest {
                
                override val subtypeName = "LightweightTopology4Cell"
                
                override def unmarshal(json: String): LightArea = json.parseJson.convertTo[LightArea]
                
                override def marshal(obj: MessageContent): String = obj.asInstanceOf[LightArea].toJson.toString()
            }
            
            object Topology4CellForTest extends MessageSubtypeForTest {
                
                override val subtypeName = "Topology4Cell"
                
                override def unmarshal(json: String): AreaForCell = json.parseJson.convertTo[AreaForCell]
                
                override def marshal(obj: MessageContent): String = obj.asInstanceOf[AreaForCell].toJson.toString()
            }
            
            object Topology4UserForTest extends MessageSubtypeForTest {
                
                override val subtypeName = "Topology4User"
                
                override def unmarshal(json: String): CellForUser = json.parseJson.convertTo[CellForUser]
                
                override def marshal(obj: MessageContent): String = obj.asInstanceOf[CellForUser].toJson.toString()
            }
            
        }
        
    }
    
    object UpdateForTest extends MessageTypeForTest {
        
        override val typeName = "Update"
        
        object Subtype {
            
            object SensorsForTest extends MessageSubtypeForTest {
                
                override val subtypeName = "Sensors"
                
                override def unmarshal(json: String): SensorList = json.parseJson.convertTo[SensorList]
                
                override def marshal(obj: MessageContent): String = obj.asInstanceOf[SensorList].toJson.toString()
            }
            
            //            object PracticabilityForTest extends MessageSubtypeForTest {
            //
            //                override val subtypeName = "Practicability"
            //
            //                override def unmarshal(json: String): MessageContent = json.parseJson.convertTo[Double]
            //
            //                override def marshal(obj: MessageContent): String = obj.asInstanceOf[Double].toJson.toString()
            //            }
            
            object PositionForTest extends MessageSubtypeForTest {
                
                override val subtypeName = "Position"
                
                override def unmarshal(json: String): Point = json.parseJson.convertTo[Point]
                
                override def marshal(obj: MessageContent): String = obj.asInstanceOf[Point].toJson.toString()
            }
            
            object ActualLoadForTest extends MessageSubtypeForTest {
                
                override val subtypeName = "CellOccupation"
                
                override def unmarshal(json: String): ActualLoadUpdate = json.parseJson.convertTo[ActualLoadUpdate]
                
                override def marshal(obj: MessageContent): String = obj.asInstanceOf[ActualLoadUpdate].toJson.toString()
            }
            
            object AdminUpdateForTest extends MessageSubtypeForTest {
                
                override val subtypeName = "CellUpdate"
                
                override def unmarshal(json: String): UpdateForAdmin = json.parseJson.convertTo[UpdateForAdmin]
                
                override def marshal(obj: MessageContent): String = obj.asInstanceOf[UpdateForAdmin].toJson.toString()
            }
            
        }
        
    }
    
    implicit def MessageType2String(msg: MessageTypeForTest): String = msg.toString
    
    implicit def String2MessageType(str: String): MessageTypeForTest = MessageTypeForTest.Factory(str)
    
    object Factory {
        
        def apply(typeName: String): MessageTypeForTest = typeName.toLowerCase match {
            case t if t == InitForTest.toLowerCase => InitForTest
            case t if t == RouteForTest.toLowerCase => RouteForTest
            case t if t == AlarmForTest.toLowerCase => AlarmForTest
            case t if t == TopologyForTest.toLowerCase => TopologyForTest
            case t if t == HandshakeForTest.toLowerCase => HandshakeForTest
            case t if t == UpdateForTest.toLowerCase => UpdateForTest
            
            case _ => null
        }
    }
    
}

object TestMessageTypeForTest extends App {
    
    var jsonStr = MessageTypeForTest.UpdateForTest.Subtype.SensorsForTest
        .marshal(SensorList(
            InfoCell(0, "uri", "name",
                Coordinates(Point(1, 1), Point(-1, -1), Point(-1, 1), Point(1, -1)),
                Point(0, 0)
            ),
            List(Sensor(1, 2.0), Sensor(2, 1.55))
        ))
    
    println(jsonStr)
    
    var jsonObj = MessageType.Update.Subtype.Sensors.unmarshal(jsonStr)
    
    println(jsonObj)
    
    val msg = AriadneLocalMessageForTest(
        UpdateForTest,
        UpdateForTest.Subtype.SensorsForTest,
        Location.Cell >> Location.Server,
        jsonObj
    )
    
    println(msg.subtype.marshal(msg.content))
    
}