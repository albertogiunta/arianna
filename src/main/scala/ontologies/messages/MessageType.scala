package ontologies.messages

import ontologies.messages.AriannaJsonProtocol._
import ontologies.messages.Location._
import ontologies.messages.MessageType.Update
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
    
            /**
              * Takes a *** object as MessageContent
              */
            object Escape {
    
                object Request extends MessageSubtype {
                    override val subtypeName = "EscapeRequest"
        
                    override val superType = Route
        
                    override def unmarshal(json: String): EscapeRequest = json.parseJson.convertTo[EscapeRequest]
        
                    override def marshal(obj: MessageContent): String = obj.asInstanceOf[EscapeRequest].toJson.toString()
                }
    
                object Response extends MessageSubtype {
                    override val subtypeName = "EscapeResponse"
        
                    override val superType = Route
        
                    override def unmarshal(json: String): EscapeResponse = json.parseJson.convertTo[EscapeResponse]
        
                    override def marshal(obj: MessageContent): String = obj.asInstanceOf[EscapeResponse].toJson.toString()
                }
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
            object Topology4CellLight extends MessageSubtype {

                override val subtypeName = "LightweightTopology4Cell"

                override val superType = Topology

                override def unmarshal(json: String): LightArea = json.parseJson.convertTo[LightArea]

                override def marshal(obj: MessageContent): String = obj.asInstanceOf[LightArea].toJson.toString()
            }
        
            /**
              * Takes a AreaForCell object as MessageContent
              */
            object Topology4Cell extends MessageSubtype {

                override val subtypeName = "Topology4Cell"

                override val superType = Topology

                override def unmarshal(json: String): AreaForCell = json.parseJson.convertTo[AreaForCell]

                override def marshal(obj: MessageContent): String = obj.asInstanceOf[AreaForCell].toJson.toString()
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

                override def unmarshal(json: String): SensorList = json.parseJson.convertTo[SensorList]

                override def marshal(obj: MessageContent): String = obj.asInstanceOf[SensorList].toJson.toString()
            }
    
            /**
              * Takes a LightCell object as MessageContent
              */
            object Practicability extends MessageSubtype {

                override val subtypeName = "Practicability"

                override val superType = Update

                override def unmarshal(json: String): LightCell = json.parseJson.convertTo[LightCell]

                override def marshal(obj: MessageContent): String = obj.asInstanceOf[LightCell].toJson.toString()
            }
    
            /**
              * Takes a ActualLoadUpdate object as MessageContent
              */
            object ActualLoad extends MessageSubtype {

                override val subtypeName = "CellOccupation"

                override val superType = Update

                override def unmarshal(json: String): ActualLoadUpdate = json.parseJson.convertTo[ActualLoadUpdate]

                override def marshal(obj: MessageContent): String = obj.asInstanceOf[ActualLoadUpdate].toJson.toString()
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

    val msg = AriadneMessage(
        Update,
        Update.Subtype.Sensors,
        Location.Cell >> Location.Server,
        jsonObj
    )

    println(msg.subtype.marshal(msg.content))

}