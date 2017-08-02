package ontologies.messages

import spray.json.{DefaultJsonProtocol, JsValue, RootJsonFormat}

/**
  * Created by Xander_C on 03/07/2017.
  */

object AriannaJsonProtocol extends DefaultJsonProtocol {
    implicit val emptyFormat: RootJsonFormat[Empty] = jsonFormat0(Empty)
    implicit val pointFormat: RootJsonFormat[Point] = jsonFormat2(Point.apply)
    implicit val coordinatesFormat: RootJsonFormat[Coordinates] = jsonFormat4(Coordinates)
    implicit val infoCellFormat: RootJsonFormat[InfoCell] = jsonFormat6((id: Int, uri: String, port: Int, name: String, roomVertices: Coordinates, antennaPosition: Point) => InfoCell.apply(id, uri, port, name, roomVertices, antennaPosition))
    implicit val passageFormat: RootJsonFormat[Passage] = jsonFormat3(Passage)
    implicit val sensorFormat: RootJsonFormat[SensorInfo] = jsonFormat2(SensorInfo)
    implicit val thresholdJsonFormat: RootJsonFormat[ThresholdInfo] = new RootJsonFormat[ThresholdInfo] {
        override def write(c: ThresholdInfo) = c match {
            case c: SingleThresholdInfo => singleThreshold.write(c)
            case c: DoubleThresholdInfo => doubleThreshold.write(c)
        }

        override def read(value: JsValue) = {
            value.asJsObject.fields.size match {
                case 1 => singleThreshold.read(value)
                case 2 => doubleThreshold.read(value)
            }
        }
    }
    implicit val sensorFormatFromConfig: RootJsonFormat[SensorInfoFromConfig] = jsonFormat4(SensorInfoFromConfig)
    implicit val cellConfig: RootJsonFormat[CellConfig] = jsonFormat2(CellConfig)
    implicit val singleThreshold: RootJsonFormat[SingleThresholdInfo] = jsonFormat1(SingleThresholdInfo)
    implicit val doubleThreshold: RootJsonFormat[DoubleThresholdInfo] = jsonFormat2(DoubleThresholdInfo)
    implicit val sensorsUpdateFormat: RootJsonFormat[SensorsInfoUpdate] = jsonFormat2(SensorsInfoUpdate.apply)
    implicit val cellFormat: RootJsonFormat[Cell] = jsonFormat10(ontologies.messages.Cell)
    implicit val areaFormat: RootJsonFormat[Area] = jsonFormat2(Area)
    implicit val cellForUserFormat: RootJsonFormat[CellForUser] = jsonFormat4(CellForUser.apply)
    implicit val cellViewedFromACellFormat: RootJsonFormat[CellViewedFromACell] = jsonFormat7(CellViewedFromACell.apply)
    implicit val areaViewedFromACellFormat: RootJsonFormat[AreaViewedFromACell] = jsonFormat2(AreaViewedFromACell.apply)
    implicit val cellUpdateFormat: RootJsonFormat[CellDataUpdate] = jsonFormat3(CellDataUpdate.apply)
    implicit val updateForAdminFormat: RootJsonFormat[UpdateForAdmin] = jsonFormat1(UpdateForAdmin)
    implicit val currentPeopleUpdateFormat: RootJsonFormat[CurrentPeopleUpdate] = jsonFormat2(CurrentPeopleUpdate.apply)
    implicit val alarmContentFormat: RootJsonFormat[AlarmContent] = jsonFormat3(AlarmContent.apply)
    implicit val greetingsFormat: RootJsonFormat[Greetings] = jsonFormat1(Greetings)
    implicit val practicabilityUpdateFormat: RootJsonFormat[PracticabilityUpdate] = jsonFormat2(PracticabilityUpdate.apply)
    implicit val areaPracticabilityFormat: RootJsonFormat[AreaPracticability] = jsonFormat2(AreaPracticability.apply)
    implicit val routeRequestFormat: RootJsonFormat[RouteRequest] = jsonFormat4(RouteRequest)
    implicit val routeInfoFormat: RootJsonFormat[RouteInfo] = jsonFormat2(RouteInfo)
    implicit val routeResponseFormat: RootJsonFormat[RouteResponse] = jsonFormat2(RouteResponse)
    implicit val routeResponseShortFormat: RootJsonFormat[RouteResponseShort] = jsonFormat1(RouteResponseShort)
    implicit val userAndAntennaPositionUpdateFormat: RootJsonFormat[UserAndAntennaPositionUpdate] = jsonFormat2(UserAndAntennaPositionUpdate)
    implicit val antennaPositionsFormat: RootJsonFormat[AntennaPositions] = jsonFormat2(AntennaPositions)
    implicit val cellForSwitcherFormat: RootJsonFormat[CellForSwitcher] = jsonFormat2(CellForSwitcher.apply)
    implicit val cellForChartFormat: RootJsonFormat[CellForChart] = jsonFormat2(CellForChart)
    implicit val cellForViewFormat: RootJsonFormat[CellForView] = jsonFormat4(CellForView)
}
