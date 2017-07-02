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

        object ToSwitcher {

            final case class SETUP_FIRST_ANTENNA_POSITION(antennaPosition: Point)

            final case class GET_BEST_NEW_CANDIDATE(bestCandidate: InfoCell)

            case class ASK_BEST_NEW_CANDIDATE(neighbors: List[InfoCell])

        }

    }

    object FromInterface {

        object ToAdmin {

            final case class MAP_CONFIG(area: Area)

            val ALARM = "alarm"
        }

    }

    object FromMovement {

        object ToMovementGenerator {
            val START = "START"
        }

        object ToSwitcher {

            final case class NEW_USER_POSITION(userPosition: Point)

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

    object FromSwitcher {

        object ToUser {

            final case class SWITCH_CELL(newCellInfo: InfoCell)
        }

        object ToPowerSupply {

            final case class CALCULATE_STRENGTH_AFTER_POSITION_CHANGED(userPosition: Point, antennaPosition: Point)

            final case class CONNECT_TO_CLOSEST_SOURCE(userPosition: Point, antennaPositions: List[InfoCell])
        }
    }

    object FromPowerSupply {

        object ToSwitcher {
            val SIGNAL_STRONG = "strong"
            val SIGNAL_MEDIUM = "medium"
            val SIGNAL_LOW = "low"
            val SIGNAL_ABSENT = "absent"
        }

    }

}