package ontologies.messages

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
    implicit val cellFormat: RootJsonFormat[Cell] = jsonFormat10(Cell)
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
            
            object Basic extends MessageSubtype { // To be renamed Greetings
                
                override val subtypeName = "Init" // Greetings
                
                def unmarshal(json: String): Greetings = null
                
                def marshal(obj: Greetings): String = null
            }
            
        }
        
    }
    
    object Alarm extends MessageType {
        
        override val typeName = "Alarm"
        
        object Subtype {
            
            object Basic extends MessageSubtype {
                
                override val subtypeName = "Alarm"
                
                def unmarshal(json: String): AlarmContent = json.parseJson.convertTo[AlarmContent]
                
                def marshal(obj: AlarmContent): String = obj.toJson.toString()
            }
            
        }
        
    }
    
    object Handshake extends MessageType {
        
        override val typeName = "Handshake"
        
        object Subtype {
            
            object Basic extends MessageSubtype {
                
                override val subtypeName = "Handshake"
                
                def unmarshal(json: String): Int = json.parseJson.convertTo[Int]
                
                def marshal(obj: Int): String = obj.toJson.toString()
            }
            
            object Cell2User extends MessageSubtype {
                
                override val subtypeName = "Cell2User"
                
                def unmarshal(json: String): CellForUser = json.parseJson.convertTo[CellForUser]
                
                def marshal(obj: CellForUser): String = obj.toJson.toString()
            }
            
            object Cell2Master extends MessageSubtype {
                
                override val subtypeName = "Cell2Master"
                
                def unmarshal(json: String): InfoCell = json.parseJson.convertTo[InfoCell]
                
                def marshal(obj: InfoCell): String = obj.toJson.toString()
            }
            
        }
        
    }
    
    object Route extends MessageType {
        
        override val typeName = "Route"
        
        object Subtype {
            
            object Basic extends MessageSubtype {
                
                override val subtypeName = "SimpleRoute"
                
                def unmarshal(json: String): Any = null
                
                def marshal(obj: Any): String = null
            }
            
            object Escape extends MessageSubtype {
                
                override val subtypeName = "EscapeRoute"
                
                def unmarshal(json: String): Any = null
                
                def marshal(obj: Any): String = null
            }
            
        }
        
    }
    
    object Topology extends MessageType {
        
        override val typeName = "Topology"
        
        object Subtype {
            
            object Planimetrics extends MessageSubtype {
                
                override val subtypeName = "Planimetrics"
                
                def unmarshal(json: String): Area = json.parseJson.convertTo[Area]
                
                def marshal(obj: Area): String = obj.toJson.toString()
            }
            
            
            object Topology4CellLight extends MessageSubtype {
                
                override val subtypeName = "LightweightTopology4Cell"
                
                def unmarshal(json: String): LightArea = json.parseJson.convertTo[LightArea]
                
                def marshal(obj: LightArea): String = obj.toJson.toString()
            }
            
            object Topology4Cell extends MessageSubtype {
                
                override val subtypeName = "Topology4Cell"
                
                def unmarshal(json: String): AreaForCell = json.parseJson.convertTo[AreaForCell]
                
                def marshal(obj: AreaForCell): String = obj.toJson.toString()
            }
            
            object Topology4User extends MessageSubtype {
                
                override val subtypeName = "Topology4User"
                
                def unmarshal(json: String): CellForUser = json.parseJson.convertTo[CellForUser]
                
                def marshal(obj: CellForUser): String = obj.toJson.toString()
            }
            
        }
        
    }
    
    object Update extends MessageType {
        
        override val typeName = "Update"
        
        object Subtype {
            
            object Sensors extends MessageSubtype {
                
                override val subtypeName = "Sensors"
                
                def unmarshal(json: String): SensorList = json.parseJson.convertTo[SensorList]
                
                def marshal(obj: SensorList): String = obj.toJson.toString()
            }
            
            object Practicability extends MessageSubtype {
                
                override val subtypeName = "Practicability"
                
                def unmarshal(json: String): Double = json.parseJson.convertTo[Double]
                
                def marshal(obj: Double): String = obj.toJson.toString()
            }
            
            object Position extends MessageSubtype {
                
                override val subtypeName = "Position"
                
                def unmarshal(json: String): Point = json.parseJson.convertTo[Point]
                
                def marshal(obj: Point): String = obj.toJson.toString()
            }
            
            object ActualLoad extends MessageSubtype {
                
                override val subtypeName = "ActualCellOccupation"
                
                def unmarshal(json: String): ActualLoadUpdate = json.parseJson.convertTo[ActualLoadUpdate]
                
                def marshal(obj: ActualLoadUpdate): String = obj.toJson.toString()
            }
            
            object AdminUpdate extends MessageSubtype {
                
                override val subtypeName = "AdminUpdate"
                
                def unmarshal(json: String): UpdateForAdmin = json.parseJson.convertTo[UpdateForAdmin]
                
                def marshal(obj: UpdateForAdmin): String = obj.toJson.toString()
            }
            
        }
        
    }
    
    implicit def MessageType2String(msg: MessageType): String = msg.toString
    
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
    
    var sensors: SensorList = MessageType.Update.Subtype.Sensors
        .unmarshal(
            "{" +
                "\"info\" : {" +
                "\"id\": 1, " +
                "\"uri\": \"akka.tcp://cellSystem@127.0.0.1:4552/user/cell1\", " +
                "\"name\": \"name\"," +
                "\"roomVertices\": { " +
                "\"northWest\": { " +
                "\"x\": 0, " +
                "\"y\": 5" +
                "}," +
                "\"northEast\": { " +
                "\"x\": 2, " +
                "\"y\": 5" +
                "}, " +
                "\"southWest\": {" +
                "\"x\": 0, " +
                "\"y\": 3" +
                "}, " +
                "\"southEast\": {" +
                "\"x\": 2, " +
                "\"y\": 3" +
                "}" +
                "}, " +
                "\"antennaPosition\": { \"x\": 0, \"y\": 1} " +
                "}, " +
                "\"sensors\": [" +
                "{\"category\" : 1,\"value\" : 2.0}, " +
                "{\"category\" : 1,\"value\" : 2.0}" +
                "]" +
                "}")
    
    var str: String = MessageType.Update.Subtype.Sensors.marshal(sensors)
    
    println(str)
    
}