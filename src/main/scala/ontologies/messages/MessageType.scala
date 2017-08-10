package ontologies.messages

import ontologies.messages.AriannaJsonProtocol._
import spray.json._

trait MessageType {

    val typeName: String

    override def toString: String = typeName
    
    override def equals(obj: scala.Any): Boolean = obj match {
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
    
                override val superType: MessageType = Init
    
                override def unmarshal(json: String): Greetings =
                    json.parseJson.convertTo[Greetings]
    
                override def marshal(obj: MessageContent): String =
                    obj.asInstanceOf[Greetings].toJson.toString()
            }
    
            /**
              *
              */
            object Goodbyes extends MessageSubtype {
                override val superType: MessageType = Init
                override val subtypeName: String = "Goodbyes"
        
                override def unmarshal(json: String): Empty =
                    json.parseJson.convertTo[Empty]
        
                override def marshal(obj: MessageContent): String =
                    obj.asInstanceOf[Empty].toJson.toString()
            }
        }

    }
    
    object Error extends MessageType {
        override val typeName = "Error"
        
        object Subtype {
    
            object Generic extends MessageSubtype {
    
                override val superType: MessageType = Error
                override val subtypeName = "Generic"
        
                override def unmarshal(json: String): Empty =
                    json.parseJson.convertTo[Empty]
        
                override def marshal(obj: MessageContent): String =
                    obj.asInstanceOf[Empty].toJson.toString()
            }
            
            object LookingForAMap extends MessageSubtype {
    
                override val superType: MessageType = Error
                override val subtypeName = "LookingForAMap"
                
                override def unmarshal(json: String): Empty =
                    json.parseJson.convertTo[Empty]
                
                override def marshal(obj: MessageContent): String =
                    obj.asInstanceOf[Empty].toJson.toString()
            }
            
            object MapIdentifierMismatch extends MessageSubtype {
    
                override val superType: MessageType = Error
                override val subtypeName = "MapIdentifierMismatch"
                
                override def unmarshal(json: String): Empty =
                    json.parseJson.convertTo[Empty]
                
                override def marshal(obj: MessageContent): String =
                    obj.asInstanceOf[Empty].toJson.toString()
            }
    
            object CellMappingMismatch extends MessageSubtype {
    
                override val superType: MessageType = Error
                override val subtypeName = "CellMappingMismatch"
        
                override def unmarshal(json: String): Empty =
                    json.parseJson.convertTo[Empty]
        
                override def marshal(obj: MessageContent): String =
                    obj.asInstanceOf[Empty].toJson.toString()
            }
            
        }
        
    }

    object Alarm extends MessageType {

        override val typeName = "Alarm"

        object Subtype {
    
            /**
              * Takes a *** object as MessageContent
              */
            object FromCell extends MessageSubtype {

                override val subtypeName = "Alarm"
    
                override val superType: MessageType = Alarm

                override def unmarshal(json: String): AlarmContent =
                    json.parseJson.convertTo[AlarmContent]

                override def marshal(obj: MessageContent): String =
                    obj.asInstanceOf[AlarmContent].toJson.toString()
            }

            object FromInterface extends MessageSubtype {
                override val subtypeName: String = "FromInterface"

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
              * Takes a Empty object as MessageContent
              */
            object UserToCell extends MessageSubtype {
    
                override val subtypeName = "UserToCell"
    
                override val superType: MessageType = Handshake
    
                override def unmarshal(json: String): Empty =
                    json.parseJson.convertTo[Empty]
    
                override def marshal(obj: MessageContent): String =
                    obj.asInstanceOf[Empty].toJson.toString()
            }
    
            /**
              * Takes a *** object as MessageContent
              */
            object CellToUser extends MessageSubtype {
    
                override val subtypeName = "UserToCell"
    
                override val superType: MessageType = Handshake
    
                override def unmarshal(json: String): RoomViewedFromAUser =
                    json.parseJson.convertTo[RoomViewedFromAUser]
    
                override def marshal(obj: MessageContent): String =
                    obj.asInstanceOf[RoomViewedFromAUser].toJson.toString()
            }
    
            /**
              * Takes a *** object as MessageContent
              */
            object CellToMaster extends MessageSubtype {
    
                override val subtypeName = "CellToMaster"
    
                override val superType: MessageType = Handshake
    
                override def unmarshal(json: String): SensorsInfoUpdate =
                    json.parseJson.convertTo[SensorsInfoUpdate]
    
                override def marshal(obj: MessageContent): String =
                    obj.asInstanceOf[SensorsInfoUpdate].toJson.toString()
            }
    
            object Acknowledgement extends MessageSubtype {
        
                override val subtypeName = "Acknowledgement"
    
                override val superType: MessageType = Handshake
    
                override def unmarshal(json: String): CellInfo =
                    json.parseJson.convertTo[CellInfo]
    
                override def marshal(obj: MessageContent): String =
                    obj.asInstanceOf[CellInfo].toJson.toString()
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
    
                override val subtypeName = "Request"
    
                override val superType: MessageType = Route
    
                override def unmarshal(json: String): MessageContent =
                    json.parseJson.convertTo[RouteRequest]
    
                override def marshal(obj: MessageContent): String =
                    obj.asInstanceOf[RouteRequest].toJson.toString()
            }
    
            object Response extends MessageSubtype {
    
                override val subtypeName = "Response"
    
                override val superType: MessageType = Route
    
                override def unmarshal(json: String): MessageContent =
                    json.parseJson.convertTo[RouteResponse]
    
                override def marshal(obj: MessageContent): String =
                    obj.asInstanceOf[RouteResponse].toJson.toString()
            }
    
            object Info extends MessageSubtype {
    
                override val subtypeName = "Info"
    
                override val superType: MessageType = Route
    
                override def unmarshal(json: String): MessageContent =
                    json.parseJson.convertTo[RouteInfo]
    
                override def marshal(obj: MessageContent): String =
                    obj.asInstanceOf[RouteInfo].toJson.toString()
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
    
                override val superType: MessageType = Topology
    
                override def unmarshal(json: String): Area =
                    json.parseJson.convertTo[Area]
    
                override def marshal(obj: MessageContent): String =
                    obj.asInstanceOf[Area].toJson.toString()
            }
        
            /**
              * Takes a LightArea object as MessageContent
              */
            object Practicabilities extends MessageSubtype {
    
                override val subtypeName = "Practicabilities"
    
                override val superType: MessageType = Topology
    
                override def unmarshal(json: String): AreaPracticability =
                    json.parseJson.convertTo[AreaPracticability]
    
                override def marshal(obj: MessageContent): String =
                    obj.asInstanceOf[AreaPracticability].toJson.toString()
            }
        
            /**
              * Takes a AreaForCell object as MessageContent
              */
            object ViewedFromACell extends MessageSubtype {
    
                override val superType: MessageType = Topology
                
                override val subtypeName = "ViewedFromACell"
    
                override def unmarshal(json: String): AreaViewedFromACell =
                    json.parseJson.convertTo[AreaViewedFromACell]
    
                override def marshal(obj: MessageContent): String =
                    obj.asInstanceOf[AreaViewedFromACell].toJson.toString()
            }
        
            /**
              * Takes a CellForUser object as MessageContent
              */
            object Topology4User extends MessageSubtype {
    
                override val superType: MessageType = Topology
                
                override val subtypeName = "Topology4User"
    
                override def unmarshal(json: String): RoomViewedFromAUser =
                    json.parseJson.convertTo[RoomViewedFromAUser]
    
                override def marshal(obj: MessageContent): String =
                    obj.asInstanceOf[RoomViewedFromAUser].toJson.toString()
            }
    
            object Acknowledgement extends MessageSubtype {
        
                override val superType: MessageType = Topology
        
                override val subtypeName: String = "Acknowledgement"
        
                override def unmarshal(json: String): Empty =
                    json.parseJson.convertTo[Empty]
        
                override def marshal(obj: MessageContent): String =
                    obj.asInstanceOf[Empty].toJson.toString()
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
    
                override val superType: MessageType = Update
    
                override def unmarshal(json: String): SensorsInfoUpdate =
                    json.parseJson.convertTo[SensorsInfoUpdate]
    
                override def marshal(obj: MessageContent): String =
                    obj.asInstanceOf[SensorsInfoUpdate].toJson.toString()
            }

            /**
              * Takes a LightCell object as MessageContent
              */
            object Practicability extends MessageSubtype {

                override val subtypeName = "Practicability"
    
                override val superType: MessageType = Update
    
                override def unmarshal(json: String): PracticabilityUpdate =
                    json.parseJson.convertTo[PracticabilityUpdate]
    
                override def marshal(obj: MessageContent): String =
                    obj.asInstanceOf[PracticabilityUpdate].toJson.toString()
            }

            /**
              * Takes a ActualLoadUpdate object as MessageContent
              */
            object CurrentPeople extends MessageSubtype {
    
                override val subtypeName = "CurrentPeople"
    
                override val superType: MessageType = Update
    
                override def unmarshal(json: String): CurrentPeopleUpdate =
                    json.parseJson.convertTo[CurrentPeopleUpdate]
    
                override def marshal(obj: MessageContent): String =
                    obj.asInstanceOf[CurrentPeopleUpdate].toJson.toString()
            }

            /**
              * Takes a UpdateForAdmin object as MessageContent
              */
            object Admin extends MessageSubtype {

                override val superType: MessageType = Update
                override val subtypeName = "Admin"
    
                override def unmarshal(json: String): AdminUpdate =
                    json.parseJson.convertTo[AdminUpdate]
    
                override def marshal(obj: MessageContent): String =
                    obj.asInstanceOf[AdminUpdate].toJson.toString()
            }

            object Position {

                /**
                  * Takes a Point object as MessageContent
                  */
                object UserPosition extends MessageSubtype {

                    override val subtypeName = "Position"
    
                    override val superType: MessageType = Update
    
                    override def unmarshal(json: String): Point =
                        json.parseJson.convertTo[Point]
    
                    override def marshal(obj: MessageContent): String =
                        obj.asInstanceOf[Point].toJson.toString()
                }

                /**
                  * Takes a Point object as MessageContent
                  */
                object AntennaPosition extends MessageSubtype {

                    override val superType: MessageType = Update
                    override val subtypeName = "AntennaPosition"
    
                    override def unmarshal(json: String): Point =
                        json.parseJson.convertTo[Point]
    
                    override def marshal(obj: MessageContent): String =
                        obj.asInstanceOf[Point].toJson.toString()
                }

            }

        }

    }

    object Interface extends MessageType {

        override val typeName = "Interface"

        object Subtype {

            /**
              * Takes a CellForChart object as MessageContent
              */
            object OpenChart extends MessageSubtype {

                override val superType: MessageType = Interface

                override val subtypeName: String = "OpenChart"
    
                override def unmarshal(json: String): MessageContent =
                    json.parseJson.convertTo[CellForChart]
    
                override def marshal(obj: MessageContent): String =
                    obj.asInstanceOf[CellForChart].toJson.toString()
            }

            /**
              * Takes a CellForView object as MessageContent
              */
            object UpdateChart extends MessageSubtype {

                override val superType: MessageType = Interface

                override val subtypeName: String = "UpdateChart"
    
                override def unmarshal(json: String): MessageContent =
                    json.parseJson.convertTo[CellForView]
    
                override def marshal(obj: MessageContent): String =
                    obj.asInstanceOf[CellForView].toJson.toString()

            }

            /**
              * Takes a InfoCell object as MessageContent
              */
            object CloseChart extends MessageSubtype {

                override val superType: MessageType = Interface

                override val subtypeName: String = "CloseChart"
    
                override def unmarshal(json: String): MessageContent =
                    json.parseJson.convertTo[CellInfo]
    
                override def marshal(obj: MessageContent): String =
                    obj.asInstanceOf[CellInfo].toJson.toString()
            }

        }

    }

    object Info extends MessageType {

        override val typeName = "Info"

        object Subtype {

            /**
              * A generic info request that an actor 'A' do to another actor 'B' to ask the information that it needs
              * in order to complete a specific job
              **/
            object Request extends MessageSubtype {

                override val subtypeName = "InfoRequest"
    
                override val superType: MessageType = Info
    
                override def unmarshal(json: String): Empty =
                    json.parseJson.convertTo[Empty]
    
                override def marshal(obj: MessageContent): String =
                    obj.asInstanceOf[Empty].toJson.toString()
            }

            /**
              * A generic info response of actor 'B' after a request from the actor 'A'
              **/
            object Response extends MessageSubtype {

                override val subtypeName = "InfoResponse"
    
                override val superType: MessageType = Info
    
                override def unmarshal(json: String): Empty =
                    json.parseJson.convertTo[Empty]
    
                override def marshal(obj: MessageContent): String =
                    obj.asInstanceOf[Empty].toJson.toString()
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
            case t if t == Interface.toLowerCase => Interface
            case t if t == Info.toLowerCase => Info

            case _ => null
        }
    }

}