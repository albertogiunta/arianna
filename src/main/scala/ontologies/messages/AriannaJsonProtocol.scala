package ontologies.messages

import spray.json.{DefaultJsonProtocol, RootJsonFormat}

/**
  * Created by Xander_C on 03/07/2017.
  */

object AriannaJsonProtocol extends DefaultJsonProtocol {
    implicit val emptyFormat: RootJsonFormat[Empty] = jsonFormat0(Empty)
    implicit val pointFormat: RootJsonFormat[Point] = jsonFormat2(Point)
    implicit val coordinatesFormat: RootJsonFormat[Coordinates] = jsonFormat4(Coordinates)
    implicit val infoCellFormat: RootJsonFormat[InfoCell] = jsonFormat5(InfoCell)
    implicit val passageFormat: RootJsonFormat[Passage] = jsonFormat3(Passage)
    implicit val sensorFormat: RootJsonFormat[Sensor] = jsonFormat4(Sensor)
    implicit val sensorListFormat: RootJsonFormat[SensorList] = jsonFormat2(SensorList.apply)
    implicit val cellFormat: RootJsonFormat[Cell] = jsonFormat10(ontologies.messages.Cell)
    implicit val areaFormat: RootJsonFormat[Area] = jsonFormat2(Area)
    implicit val cellForUserFormat: RootJsonFormat[CellForUser] = jsonFormat4(CellForUser.apply)
    implicit val cellForCellFormat: RootJsonFormat[CellForCell] = jsonFormat7(CellForCell.apply)
    implicit val areaForCellFormat: RootJsonFormat[AreaForCell] = jsonFormat2(AreaForCell.apply)
    implicit val cellUpdateFormat: RootJsonFormat[CellUpdate] = jsonFormat3(CellUpdate.apply)
    implicit val updateForAdminFormat: RootJsonFormat[UpdateForAdmin] = jsonFormat1(UpdateForAdmin)
    implicit val actualLoadUpdateFormat: RootJsonFormat[ActualLoadUpdate] = jsonFormat2(ActualLoadUpdate.apply)
    implicit val alarmContentFormat: RootJsonFormat[AlarmContent] = jsonFormat3(AlarmContent.apply)
    implicit val greetingsFormat: RootJsonFormat[Greetings] = jsonFormat1(Greetings)
    implicit val lightCellFormat: RootJsonFormat[PracticabilityUpdate] = jsonFormat2(PracticabilityUpdate.apply)
    implicit val lightAreaFormat: RootJsonFormat[AreaPracticability] = jsonFormat2(AreaPracticability.apply)
    implicit val routeRequestFormat: RootJsonFormat[RouteRequest] = jsonFormat3(RouteRequest)
    implicit val routeInfoFormat: RootJsonFormat[RouteInfo] = jsonFormat2(RouteInfo)
    implicit val routeResponseFormat: RootJsonFormat[RouteResponse] = jsonFormat2(RouteResponse)
    implicit val escapeRequestFormat: RootJsonFormat[EscapeRequest] = jsonFormat2(EscapeRequest)
    implicit val escapeResponseFormat: RootJsonFormat[EscapeResponse] = jsonFormat2(EscapeResponse)
    implicit val userAndAntennaPositionUpdateFormat: RootJsonFormat[UserAndAntennaPositionUpdate] = jsonFormat2(UserAndAntennaPositionUpdate)
    implicit val antennaPositionsFormat: RootJsonFormat[AntennaPositions] = jsonFormat2(AntennaPositions)
    implicit val cellForSwitcherFormat: RootJsonFormat[CellForSwitcher] = jsonFormat2(CellForSwitcher.apply)
}
