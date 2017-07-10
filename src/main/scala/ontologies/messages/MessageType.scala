package ontologies.messages

import ontologies.messages.Location._
import ontologies.messages.MessageType._
import spray.json.{DefaultJsonProtocol, _}

/**
  * Created by Xander_C on 03/07/2017.
  */

object AriannaJsonProtocol extends DefaultJsonProtocol {
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

trait MessageType {
    
    val typeName: String
    
    override def toString: String = typeName
    
    override def equals(obj: scala.Any) = obj match {
        case o: MessageType => o.toString == this.toString
        case o: String => o == this.toString
    }
}

object MessageType {
    
    object Init extends MessageType {
        
        override val typeName = "Init"
        
        object Subtype {
    
            object Greetings extends MessageSubtype { // To be renamed Greetings
        
                override val subtypeName = "Greetings" // Greetings
        
                override val superType = Init
        
                override def unmarshal(json: String): Greetings = null
        
                override def marshal(obj: MessageContent): String = null
            }
            
        }
        
    }
    
    object Alarm extends MessageType {
        
        override val typeName = "Alarm"
        
        object Subtype {
            
            object Basic extends MessageSubtype {
                
                override val subtypeName = "Alarm"
    
                override val superType = Alarm
    
                override def unmarshal(json: String): AlarmContent =
                    json.parseJson.convertTo[AlarmContent]
    
                override def marshal(obj: MessageContent): String =
                    obj.asInstanceOf[AlarmContent].toJson.toString()
            }
            
        }
        
    }
    
    object Handshake extends MessageType {
        
        override val typeName = "Handshake"
        
        object Subtype {
            
            object Cell2User extends MessageSubtype {
                
                override val subtypeName = "Cell2User"
    
                override val superType = Handshake
    
                override def unmarshal(json: String): CellForUser = json.parseJson.convertTo[CellForUser]
    
                override def marshal(obj: MessageContent): String = obj.asInstanceOf[CellForUser].toJson.toString()
            }
            
            object Cell2Master extends MessageSubtype {
                
                override val subtypeName = "Cell2Master"
    
                override val superType = Handshake
    
                override def unmarshal(json: String): InfoCell = json.parseJson.convertTo[InfoCell]
    
                override def marshal(obj: MessageContent): String = obj.asInstanceOf[InfoCell].toJson.toString()
            }
            
        }
        
    }
    
    object Route extends MessageType {
        
        override val typeName = "Route"
        
        object Subtype {
            
            object Basic extends MessageSubtype {
    
                override val subtypeName = "Simple"
    
                override val superType = Route
    
                override def unmarshal(json: String): MessageContent = null
    
                override def marshal(obj: MessageContent): String = null
            }
            
            object Escape extends MessageSubtype {
    
                override val subtypeName = "Escape"
    
                override val superType = Route
    
                override def unmarshal(json: String): MessageContent = null
    
                override def marshal(obj: MessageContent): String = null
            }
            
        }
        
    }
    
    object Topology extends MessageType {
        
        override val typeName = "Topology"
        
        object Subtype {
            
            object Planimetrics extends MessageSubtype {
                
                override val subtypeName = "Planimetrics"
    
                override val superType = Topology
    
                override def unmarshal(json: String): Area = json.parseJson.convertTo[Area]
    
                override def marshal(obj: MessageContent): String = obj.asInstanceOf[InfoCell].toJson.toString()
            }
            
            
            object Topology4CellLight extends MessageSubtype {
                
                override val subtypeName = "LightweightTopology4Cell"
    
                override val superType = Topology
    
                override def unmarshal(json: String): LightArea = json.parseJson.convertTo[LightArea]
    
                override def marshal(obj: MessageContent): String = obj.asInstanceOf[LightArea].toJson.toString()
            }
            
            object Topology4Cell extends MessageSubtype {
                
                override val subtypeName = "Topology4Cell"
    
                override val superType = Topology
    
                override def unmarshal(json: String): AreaForCell = json.parseJson.convertTo[AreaForCell]
    
                override def marshal(obj: MessageContent): String = obj.asInstanceOf[AreaForCell].toJson.toString()
            }
            
            object Topology4User extends MessageSubtype {
                
                override val subtypeName = "Topology4User"
    
                override val superType = Topology
    
                override def unmarshal(json: String): CellForUser = json.parseJson.convertTo[CellForUser]
    
                override def marshal(obj: MessageContent): String = obj.asInstanceOf[CellForUser].toJson.toString()
            }
            
        }
        
    }
    
    object Update extends MessageType {
        
        override val typeName = "Update"
        
        object Subtype {
            
            object Sensors extends MessageSubtype {
                
                override val subtypeName = "Sensors"
    
                override val superType = Update
    
                override def unmarshal(json: String): SensorList = json.parseJson.convertTo[SensorList]
    
                override def marshal(obj: MessageContent): String = obj.asInstanceOf[SensorList].toJson.toString()
            }
    
            object Practicability extends MessageSubtype {
        
                override val subtypeName = "Practicability"
        
                override val superType = Update
        
                override def unmarshal(json: String): LightCell = json.parseJson.convertTo[LightCell]
        
                override def marshal(obj: MessageContent): String = obj.asInstanceOf[LightCell].toJson.toString()
            }
            
            object Position extends MessageSubtype {
                
                override val subtypeName = "Position"
    
                override val superType = Update
    
                override def unmarshal(json: String): Point = json.parseJson.convertTo[Point]
    
                override def marshal(obj: MessageContent): String = obj.asInstanceOf[Point].toJson.toString()
            }
            
            object ActualLoad extends MessageSubtype {
    
                override val subtypeName = "CellOccupation"
    
                override val superType = Update
    
                override def unmarshal(json: String): ActualLoadUpdate = json.parseJson.convertTo[ActualLoadUpdate]
    
                override def marshal(obj: MessageContent): String = obj.asInstanceOf[ActualLoadUpdate].toJson.toString()
            }
            
            object AdminUpdate extends MessageSubtype {
    
                override val subtypeName = "CellUpdate"
    
                override val superType = Update
    
                override def unmarshal(json: String): UpdateForAdmin = json.parseJson.convertTo[UpdateForAdmin]
    
                override def marshal(obj: MessageContent): String = obj.asInstanceOf[UpdateForAdmin].toJson.toString()
            }
            
        }
        
    }
    
    implicit def MessageType2String(msg: MessageType): String = msg.typeName
    
    implicit def String2MessageType(str: String): MessageType = MessageType.Factory(str)
    
    object Factory {
        
        def apply(typeName: String): MessageType = typeName.toLowerCase match {
            case t if t == Init.toLowerCase => Init
            case t if t == Route.toLowerCase => Route
            case t if t == Alarm.toLowerCase => Alarm
            case t if t == Topology.toLowerCase => Topology
            case t if t == Handshake.toLowerCase => Handshake
            case t if t == Update.toLowerCase => Update

            case _ => null
        }
    }
    
}

object TestMessageType extends App {
    
    var jsonStr = MessageType.Update.Subtype.Sensors
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
    
    val msg = AriadneLocalMessage(
        Update,
        Update.Subtype.Sensors,
        Location.Cell >> Location.Server,
        jsonObj
    )
    
    println(msg.subtype.marshal(msg.content))
    
}