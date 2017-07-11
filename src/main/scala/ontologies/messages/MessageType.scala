package ontologies.messages

import spray.json.{DefaultJsonProtocol, _}

/**
  * Created by Xander_C on 03/07/2017.
  */

object AriannaJsonProtocol extends DefaultJsonProtocol {
    implicit val pointFormat = jsonFormat2(Point)
    implicit val coordinatesFormat = jsonFormat4(Coordinates)
    implicit val infoCellFormat = jsonFormat5(InfoCell)
    implicit val passageFormat = jsonFormat3(Passage)
    implicit val sensorFormat = jsonFormat2(Sensor)
    implicit val cellFormat = jsonFormat10(Cell)
    implicit val areaFormat = jsonFormat2(Area)
    implicit val cellForUserFormat = jsonFormat4(CellForUser.apply)
    implicit val cellForCellFormat = jsonFormat6(CellForCell.apply)
    implicit val areaForCellFormat = jsonFormat2(AreaForCell.apply)
    implicit val cellUpdateFormat = jsonFormat3(CellUpdate.apply)
    implicit val updateForAdminFormat = jsonFormat1(UpdateForAdmin)
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

            object Basic extends MessageSubtype {

                override val subtypeName = "Init"

                //def unmarshal(json: String): Any = null

                //def marshal(obj: Any): String = null
            }

        }

    }

    object Alarm extends MessageType {

        override val typeName = "Alarm"

        object Subtype {

            object Basic extends MessageSubtype {

                override val subtypeName = "Alarm"

                def unmarshal(json: String): Int = json.parseJson.convertTo[Int]

                def marshal(obj: Int): String = obj.toJson.toString()
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


            object RealTopology extends MessageSubtype {

                override val subtypeName = "RealTopology"

                def unmarshal(json: String): Area = json.parseJson.convertTo[Area]

                def marshal(obj: Area): String = obj.toJson.toString()
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

    object Movement extends MessageType {

        override val typeName = "Movement"

        object Subtype {

            object Up extends MessageSubtype {

                override val subtypeName = "Up"

                //                def unmarshal(json: String): List[Sensor] = json.parseJson.convertTo[List[Sensor]]
                //
                //                def marshal(obj: List[Sensor]): String = obj.toJson.toString()
            }

            object Down extends MessageSubtype {

                override val subtypeName = "Down"

                //                def unmarshal(json: String): List[Sensor] = json.parseJson.convertTo[List[Sensor]]
                //
                //                def marshal(obj: List[Sensor]): String = obj.toJson.toString()
            }

            object Left extends MessageSubtype {

                override val subtypeName = "Left"

                //                def unmarshal(json: String): List[Sensor] = json.parseJson.convertTo[List[Sensor]]
                //
                //                def marshal(obj: List[Sensor]): String = obj.toJson.toString()
            }

            object Right extends MessageSubtype {

                override val subtypeName = "Right"

                //                def unmarshal(json: String): List[Sensor] = json.parseJson.convertTo[List[Sensor]]
                //
                //                def marshal(obj: List[Sensor]): String = obj.toJson.toString()
            }

        }

    }

    object SwitcherMsg extends MessageType {

        override val typeName = "SwitcherMsg"

        object Subtype {

            object SetupFirstAntennaPosition extends MessageSubtype {

                override val subtypeName = "SetupFirstAntennaPosition"

                //                def unmarshal(json: String): List[Sensor] = json.parseJson.convertTo[List[Sensor]]
                //
                //                def marshal(obj: List[Sensor]): String = obj.toJson.toString()
            }

            object BestNexHost extends MessageSubtype {

                override val subtypeName = "GetBestNewCandidate"

                //                def unmarshal(json: String): List[Sensor] = json.parseJson.convertTo[List[Sensor]]
                //
                //                def marshal(obj: List[Sensor]): String = obj.toJson.toString()
            }

            object SwitchCell extends MessageSubtype {

                override val subtypeName = "SwtichCell"

                //                def unmarshal(json: String): List[Sensor] = json.parseJson.convertTo[List[Sensor]]
                //
                //                def marshal(obj: List[Sensor]): String = obj.toJson.toString()
            }

            object CalculateStrength extends MessageSubtype {

                override val subtypeName = "CalculateStrengthAfterPositionChanged"

                //                def unmarshal(json: String): List[Sensor] = json.parseJson.convertTo[List[Sensor]]
                //
                //                def marshal(obj: List[Sensor]): String = obj.toJson.toString()
            }

            object ScanAndFind extends MessageSubtype {

                override val subtypeName = "ConnectTOTheClosestSource"

                //                def unmarshal(json: String): List[Sensor] = json.parseJson.convertTo[List[Sensor]]
                //
                //                def marshal(obj: List[Sensor]): String = obj.toJson.toString()
            }


        }

    }


    object SignalStrength extends MessageType {

        override val typeName = "SignalStrength"

        object Subtype {

            object Strong extends MessageSubtype {

                override val subtypeName = "Strong"

                //                def unmarshal(json: String): List[Sensor] = json.parseJson.convertTo[List[Sensor]]
                //
                //                def marshal(obj: List[Sensor]): String = obj.toJson.toString()
            }

            object Medium extends MessageSubtype {

                override val subtypeName = "Medium"

                //                def unmarshal(json: String): List[Sensor] = json.parseJson.convertTo[List[Sensor]]
                //
                //                def marshal(obj: List[Sensor]): String = obj.toJson.toString()
            }

            object Low extends MessageSubtype {

                override val subtypeName = "Low"

                //                def unmarshal(json: String): List[Sensor] = json.parseJson.convertTo[List[Sensor]]
                //
                //                def marshal(obj: List[Sensor]): String = obj.toJson.toString()
            }

            object VeryLow extends MessageSubtype {

                override val subtypeName = "None"

                //                def unmarshal(json: String): List[Sensor] = json.parseJson.convertTo[List[Sensor]]
                //
                //                def marshal(obj: List[Sensor]): String = obj.toJson.toString()
            }

        }

    }


    object Update extends MessageType {

        override val typeName = "Update"

        object Subtype {

            object Sensors extends MessageSubtype {

                override val subtypeName = "Sensors"

                def unmarshal(json: String): List[Sensor] = json.parseJson.convertTo[List[Sensor]]

                def marshal(obj: List[Sensor]): String = obj.toJson.toString()
            }

            object Practicability extends MessageSubtype {

                override val subtypeName = "Practicability"

                def unmarshal(json: String): Double = json.parseJson.convertTo[Double]

                def marshal(obj: Double): String = obj.toJson.toString()
            }

            object UserPosition extends MessageSubtype {

                override val subtypeName = "Position"

                def unmarshal(json: String): Point = json.parseJson.convertTo[Point]

                def marshal(obj: Point): String = obj.toJson.toString()
            }

            object CellOccupation extends MessageSubtype {

                override val subtypeName = "CellOccupation"

                def unmarshal(json: String): Int = json.parseJson.convertTo[Int]

                def marshal(obj: Int): String = obj.toJson.toString()
            }

            object UpdateForAdmin extends MessageSubtype {

                override val subtypeName = "UpdateForAdmin"

                def unmarshal(json: String): UpdateForAdmin = json.parseJson.convertTo[UpdateForAdmin]

                def marshal(obj: UpdateForAdmin): String = obj.toJson.toString()
            }

            object AntennaPosition extends MessageSubtype {

                override val subtypeName = "AntennaPosition"

                def unmarshal(json: String): Point = json.parseJson.convertTo[Point]

                def marshal(obj: Point): String = obj.toJson.toString()
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

    var sensor: List[Sensor] = MessageType.Update.Subtype.Sensors.unmarshal("[{\"category\" : 1,\"value\" : 2.0}, {\"category\" : 1,\"value\" : 2.0}]")

    var str: String = MessageType.Update.Subtype.Sensors.marshal(sensor)

    println(str)

}