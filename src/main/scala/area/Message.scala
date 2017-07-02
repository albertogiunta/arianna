package area

object Message {

    object FromServer {

        object ToSelf {
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
            final case class MAP_CONFIG(area : Area)
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

    }

    object ToServer {

        object FromSelf {
            val START = "start"
        }

        object FromCell {

            final case class CELL_FOR_SERVER(cell: Cell)

        }

    }

    object ToCell {

        object FromSelf {
            val START = "start"
        }

        object FromUser {
            val CONNECT = "connect"
            val DISCONNECT = "disconnect"

            final case class FIND_ROUTE(startingRoomId: Int, endingRoomId: Int)

        }

        object FromServer {
            val SEND_ALARM_TO_USERS = "sendAlarmToUsers"
        }

    }

    object ToUser {

        object FromSelf {
            val START = "start"
            val STOP = "stop"

            final case class ASK_ROUTE(toRoomId: Int)

        }

        object FromCell {
            val ALARM = "alarm"

            final case class CELL_FOR_USER(cell: CellForUser)

        }

    }

}