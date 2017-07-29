package ontologies.messages

import ontologies.messages.AriannaJsonProtocol._
import ontologies.messages.Location._
import ontologies.messages.MessageType.{Movement, _}
import spray.json._

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

            /**
              * Takes a Greetings object as MessageContent
              */
            object Greetings extends MessageSubtype { // To be renamed Greetings

                override val subtypeName = "Greetings" // Greetings

                override val superType = Init

                override def unmarshal(json: String): Greetings = json.parseJson.convertTo[Greetings]

                override def marshal(obj: MessageContent): String = obj.asInstanceOf[Greetings].toJson.toString()
            }

        }

    }

    object Alarm extends MessageType {

        override val typeName = "Alarm"

        object Subtype {
    
            /**
              * Takes a *** object as MessageContent
              */
            object Basic extends MessageSubtype {

                override val subtypeName = "Alarm"

                override val superType = Alarm

                override def unmarshal(json: String): AlarmContent =
                    json.parseJson.convertTo[AlarmContent]

                override def marshal(obj: MessageContent): String =
                    obj.asInstanceOf[AlarmContent].toJson.toString()
            }

            object FromInterface extends MessageSubtype {
                override val subtypeName: String = "From Interface"

                override val superType: MessageType = Alarm

                override def unmarshal(json: String): Empty =
                    json.parseJson.convertTo[Empty]

                override def marshal(obj: MessageContent): String =
                    obj.asInstanceOf[Empty].toJson.toString()
            }

        }

    }

    object Handshake extends MessageType {

        override val typeName = "Handshake"

        object Subtype {
    
            /**
              * Takes a *** object as MessageContent
              */
            object User2Cell extends MessageSubtype {

                override val subtypeName = "User2Cell"

                override val superType = Handshake

                override def unmarshal(json: String): Empty = json.parseJson.convertTo[Empty]

                override def marshal(obj: MessageContent): String = obj.asInstanceOf[Empty].toJson.toString()
            }
    
            /**
              * Takes a *** object as MessageContent
              */
            object Cell2User extends MessageSubtype {

                override val subtypeName = "Cell2User"

                override val superType = Handshake

                override def unmarshal(json: String): CellForUser = json.parseJson.convertTo[CellForUser]

                override def marshal(obj: MessageContent): String = obj.asInstanceOf[CellForUser].toJson.toString()
            }
    
            /**
              * Takes a *** object as MessageContent
              */
            object CellToMaster extends MessageSubtype {
    
                override val subtypeName = "CellToMaster"

                override val superType = Handshake
    
                override def unmarshal(json: String): SensorsUpdate = json.parseJson.convertTo[SensorsUpdate]
    
                override def marshal(obj: MessageContent): String = obj.asInstanceOf[SensorsUpdate].toJson.toString()
            }
    
            object Acknowledgement extends MessageSubtype {
        
                override val subtypeName = "Acknowledgement"
        
                override val superType = Handshake
        
                override def unmarshal(json: String): Empty = json.parseJson.convertTo[Empty]
        
                override def marshal(obj: MessageContent): String = obj.asInstanceOf[Empty].toJson.toString()
            }
        }

    }

    object Route extends MessageType {

        override val typeName = "Route"

        object Subtype {
    
            /**
              * Takes a *** object as MessageContent
              */
            object Request extends MessageSubtype {
        
                override val subtypeName = "RouteRequest"

                override val superType = Route
        
                override def unmarshal(json: String): MessageContent = json.parseJson.convertTo[RouteRequest]
        
                override def marshal(obj: MessageContent): String = obj.asInstanceOf[RouteRequest].toJson.toString()
            }
    
            object Response extends MessageSubtype {
        
                override val subtypeName = "RouteResponse"
        
                override val superType = Route
        
                override def unmarshal(json: String): MessageContent = json.parseJson.convertTo[RouteResponse]
        
                override def marshal(obj: MessageContent): String = obj.asInstanceOf[RouteResponse].toJson.toString()
            }
    
            object Info extends MessageSubtype {
        
                override val subtypeName = "RouteInfo"
        
                override val superType = Route
        
                override def unmarshal(json: String): MessageContent = json.parseJson.convertTo[RouteInfo]
        
                override def marshal(obj: MessageContent): String = obj.asInstanceOf[RouteInfo].toJson.toString()
            }

        }

    }

    object Topology extends MessageType {

        override val typeName = "Topology"
    
        object Subtype {
        
            /**
              * Takes a Area object as MessageContent
              */
            object Planimetrics extends MessageSubtype {

                override val subtypeName = "Planimetrics"

                override val superType = Topology

                override def unmarshal(json: String): Area = json.parseJson.convertTo[Area]

                override def marshal(obj: MessageContent): String = obj.asInstanceOf[Area].toJson.toString()
            }
        
            /**
              * Takes a LightArea object as MessageContent
              */
            object Practicability extends MessageSubtype {

                override val subtypeName = "LightweightTopology4Cell"

                override val superType = Topology
    
                override def unmarshal(json: String): AreaPracticability = json.parseJson.convertTo[AreaPracticability]
    
                override def marshal(obj: MessageContent): String = obj.asInstanceOf[AreaPracticability].toJson.toString()
            }
        
            /**
              * Takes a AreaForCell object as MessageContent
              */
            object ViewedFromACell extends MessageSubtype {
    
                override val subtypeName = "ViewedFromACell"

                override val superType = Topology
    
                override def unmarshal(json: String): AreaViewedFromACell = json.parseJson.convertTo[AreaViewedFromACell]
    
                override def marshal(obj: MessageContent): String = obj.asInstanceOf[AreaViewedFromACell].toJson.toString()
            }
        
            /**
              * Takes a CellForUser object as MessageContent
              */
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
            
            /**
              * Takes a SensorList object as MessageContent
              */
            object Sensors extends MessageSubtype {
                
                override val subtypeName = "Sensors"
                
                override val superType = Update
                
                override def unmarshal(json: String): SensorsUpdate = json.parseJson.convertTo[SensorsUpdate]
                
                override def marshal(obj: MessageContent): String = obj.asInstanceOf[SensorsUpdate].toJson.toString()
            }
            
            /**
              * Takes a LightCell object as MessageContent
              */
            object Practicability extends MessageSubtype {
                
                override val subtypeName = "Practicability"
                
                override val superType = Update
                
                override def unmarshal(json: String): PracticabilityUpdate = json.parseJson.convertTo[PracticabilityUpdate]
                
                override def marshal(obj: MessageContent): String = obj.asInstanceOf[PracticabilityUpdate].toJson.toString()
            }
            
            /**
              * Takes a ActualLoadUpdate object as MessageContent
              */
            object CurrentPeople extends MessageSubtype {
                
                override val subtypeName = "CellOccupation"
                
                override val superType = Update
                
                override def unmarshal(json: String): CurrentPeopleUpdate = json.parseJson.convertTo[CurrentPeopleUpdate]
                
                override def marshal(obj: MessageContent): String = obj.asInstanceOf[CurrentPeopleUpdate].toJson.toString()
            }
            
            /**
              * Takes a UpdateForAdmin object as MessageContent
              */
            object UpdateForAdmin extends MessageSubtype {
                
                override val superType: MessageType = Update
                override val subtypeName = "UpdateForAdmin"
                
                override def unmarshal(json: String): UpdateForAdmin = json.parseJson.convertTo[UpdateForAdmin]
                
                override def marshal(obj: MessageContent): String = obj.asInstanceOf[UpdateForAdmin].toJson.toString()
            }
            
            object Position {
                
                /**
                  * Takes a Point object as MessageContent
                  */
                object UserPosition extends MessageSubtype {
                    
                    override val subtypeName = "Position"
                    
                    override val superType = Update
                    
                    override def unmarshal(json: String): Point = json.parseJson.convertTo[Point]
                    
                    override def marshal(obj: MessageContent): String = obj.asInstanceOf[Point].toJson.toString()
                }
                
                /**
                  * Takes a Point object as MessageContent
                  */
                object AntennaPosition extends MessageSubtype {
                    
                    override val superType: MessageType = Update
                    override val subtypeName = "AntennaPosition"
                    
                    override def unmarshal(json: String): Point = json.parseJson.convertTo[Point]
                    
                    override def marshal(obj: MessageContent): String = obj.asInstanceOf[Point].toJson.toString()
                }
                
            }
            
        }
        
    }

    object Movement extends MessageType {

        override val typeName = "Movement"

        object Subtype {
    
            /**
              * Takes a Empty object as MessageContent
              */
            object Up extends MessageSubtype {

                override val superType: MessageType = Movement.this
                override val subtypeName = "Up"

                override def unmarshal(json: String): Empty = json.parseJson.convertTo[Empty]

                override def marshal(obj: MessageContent): String = obj.asInstanceOf[Empty].toJson.toString()
            }
    
            /**
              * Takes a Empty object as MessageContent
              */
            object Down extends MessageSubtype {

                override val superType: MessageType = Movement.this
                override val subtypeName = "Down"

                override def unmarshal(json: String): Empty = json.parseJson.convertTo[Empty]

                override def marshal(obj: MessageContent): String = obj.asInstanceOf[Empty].toJson.toString()
            }
    
            /**
              * Takes a Empty object as MessageContent
              */
            object Left extends MessageSubtype {

                override val superType: MessageType = Movement.this
                override val subtypeName = "Left"

                override def unmarshal(json: String): Empty = json.parseJson.convertTo[Empty]

                override def marshal(obj: MessageContent): String = obj.asInstanceOf[Empty].toJson.toString()
            }
    
            /**
              * Takes a *** object as MessageContent
              */
            object Right extends MessageSubtype {

                override val superType: MessageType = Movement.this
                override val subtypeName = "Right"
        
                override def unmarshal(json: String): MessageContent = json.parseJson.convertTo[Empty]
        
                override def marshal(obj: MessageContent): String = obj.asInstanceOf[Empty].toJson.toString()
            }

        }

    }

    object SwitcherMsg extends MessageType {

        override val typeName = "SwitcherMsg"

        object Subtype {
    
            //            object SetupFirstAntennaPosition extends MessageSubtype {
            //
            //
            //                override val superType: MessageType = SwitcherMsg
            //                override val subtypeName = "SetupFirstAntennaPosition"
            //
            //                override def unmarshal(json: String): MessageContent = null
            //
            //                override def marshal(jso: MessageContent): String = null
            //            }
    
            /**
              * Takes a InfoCell object as MessageContent
              */
            object BestNexHost extends MessageSubtype {

                override val superType: MessageType = SwitcherMsg
                override val subtypeName = "GetBestNewCandidate"

                override def unmarshal(json: String): InfoCell = json.parseJson.convertTo[InfoCell]

                override def marshal(obj: MessageContent): String = obj.asInstanceOf[InfoCell].toJson.toString()
            }
    
            /**
              * Takes a InfoCell object as MessageContent
              */
            object SwitchCell extends MessageSubtype {

                override val superType: MessageType = SwitcherMsg
                override val subtypeName = "SwtichCell"

                override def unmarshal(json: String): InfoCell = json.parseJson.convertTo[InfoCell]

                override def marshal(obj: MessageContent): String = obj.asInstanceOf[InfoCell].toJson.toString()
            }
    
            /**
              * Takes a UserAndAntennaPositionUpdate object as MessageContent
              */
            object CalculateStrength extends MessageSubtype {

                override val superType: MessageType = SwitcherMsg
                override val subtypeName = "CalculateStrengthAfterPositionChanged"

                override def unmarshal(json: String): UserAndAntennaPositionUpdate = json.parseJson.convertTo[UserAndAntennaPositionUpdate]

                override def marshal(obj: MessageContent): String = obj.asInstanceOf[UserAndAntennaPositionUpdate].toJson.toString()
            }
    
            /**
              * Takes a AntennaPositions object as MessageContent
              */
            object ScanAndFind extends MessageSubtype {

                override val superType: MessageType = SwitcherMsg
                override val subtypeName = "ConnectTOTheClosestSource"

                override def unmarshal(json: String): AntennaPositions = json.parseJson.convertTo[AntennaPositions]

                override def marshal(obj: MessageContent): String = obj.asInstanceOf[AntennaPositions].toJson.toString()
            }

        }

    }
    
    
    object SignalStrength extends MessageType {

        override val typeName = "SignalStrength"

        object Subtype {
    
            /**
              * Takes a Empty object as MessageContent
              */
            object Strong extends MessageSubtype {

                override val superType: MessageType = SignalStrength
                override val subtypeName = "Strong"

                override def unmarshal(json: String): Empty = json.parseJson.convertTo[Empty]

                override def marshal(obj: MessageContent): String = obj.asInstanceOf[Empty].toJson.toString()
            }
    
            /**
              * Takes a Empty object as MessageContent
              */
            object Medium extends MessageSubtype {

                override val superType: MessageType = SignalStrength
                override val subtypeName = "Medium"

                override def unmarshal(json: String): Empty = json.parseJson.convertTo[Empty]

                override def marshal(obj: MessageContent): String = obj.asInstanceOf[Empty].toJson.toString()
            }
    
            /**
              * Takes a Empty object as MessageContent
              */
            object Low extends MessageSubtype {

                override val superType: MessageType = SignalStrength
                override val subtypeName = "Low"

                override def unmarshal(json: String): Empty = json.parseJson.convertTo[Empty]

                override def marshal(obj: MessageContent): String = obj.asInstanceOf[Empty].toJson.toString()
            }
    
            /**
              * Takes a Empty object as MessageContent
              */
            object VeryLow extends MessageSubtype {

                override val superType: MessageType = SignalStrength
                override val subtypeName = "None"

                override def unmarshal(json: String): Empty = json.parseJson.convertTo[Empty]

                override def marshal(obj: MessageContent): String = obj.asInstanceOf[Empty].toJson.toString()
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
            case t if t == Movement.toLowerCase => Movement
            case t if t == SignalStrength.toLowerCase => SignalStrength
            case t if t == SwitcherMsg.toLowerCase => SwitcherMsg

            case _ => null
        }
    }

}

object TestMessageType extends App {

    var jsonStr = MessageType.Update.Subtype.Sensors
        .marshal(SensorsUpdate(
            InfoCell(0, "uri", "name",
                Coordinates(Point(1, 1), Point(-1, -1), Point(-1, 1), Point(1, -1)),
                Point(0, 0)
            ),
            List(Sensor(1, 2.0, 0.0, 0.0), Sensor(2, 1.55, 0.0, 0.0))
        ))

    println(jsonStr)

    var jsonObj = MessageType.Update.Subtype.Sensors.unmarshal(jsonStr)

    println(jsonObj)

    val msg = AriadneMessage(
        Update,
        Update.Subtype.Sensors,
        Location.Cell >> Location.Master,
        jsonObj
    )

    println(msg.subtype.marshal(msg.content))

}