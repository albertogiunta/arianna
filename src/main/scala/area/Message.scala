package area

object Message {

    object FromServer {

        object ToSelf {
            val START = "start"
        }

        object ToNotifier {
            val START = "start"
        }

        object ToCell {
            val SEND_ALARM_TO_USERS = "sendAlarmToUsers"
        }

        object ToAdmin {
            val SEND_ALARM_TO_ADMIN = "sendAlarmToAdmin"

            final case class STATUS_UPDATE(updateForAdmin: UpdateForAdmin)

            final case class SAMPLE_UPDATE(sampleUpdate: SampleUpdate)

        }

    }

    object FromAdmin {

        object ToServer {
            val ALARM = "alarm"

            final case class MAP_CONFIG(area: Area)

        }

    }

    object FromCell {

        object ToSelf {
            val START = "start"
        }

        object ToServer {

            final case class CELL_FOR_SERVER(cell: Cell)

        }

        object ToUser {
            val ALARM = "alarm"

            final case class CELL_FOR_USER(cell: CellForUser)

        }

    }

    object FromUser {

        object ToSelf {
            val START = "start"
            val STOP = "stop"

            final case class ASK_ROUTE(toRoomId: Int)

        }

        object ToCell {
            val CONNECT = "connect"
            val DISCONNECT = "disconnect"

            final case class FIND_ROUTE(startingRoomId: Int, endingRoomId: Int)

        }

        object ToMovement {
            val START = "start"
        }

        object ToPowerSupply {

            final case class CURRENT_ROOM_ANTENNA_POSITION(antennaPosition: Point)

        }

    }

    object FromInterface {

        object ToAdmin {

            final case class MAP_CONFIG(area: Area)

            val ALARM = "alarm"
        }

    }

    object FromMovementGenerator {

        object ToMovement {
            val UP = "up"
            val DOWN = "down"
            val LEFT = "left"
            val RIGHT = "right"
        }

    }

    object FromMovement {

        object ToMovementGenerator {
            val START = "START"
        }

        object ToPowerSupply {

            final case class NEW_USER_POSITION(userPosition: Point)

        }

    }

    object FromPowerSupply {

        object ToUser {
            val SIGNAL_STRONG = "strong"
            val SIGNAL_MEDIUM = "medium"
            val SIGNAL_LOW = "low"
        }

    }

}