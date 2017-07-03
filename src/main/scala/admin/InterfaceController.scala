package admin

import java.io.File

import akka.actor.ActorRef
import area.{Area, Cell, Coordinates, InfoCell, Message, Passage, Point, SampleUpdate, Sensor}
import spray.json.{DefaultJsonProtocol, _}

import scala.io.Source
import scalafx.application.Platform

object MyJsonProtocol extends DefaultJsonProtocol {
    implicit val pointFormat = jsonFormat2(Point)
    implicit val coordinatesFormat = jsonFormat4(Coordinates)
    implicit val infoCellFormat = jsonFormat5(InfoCell)
    implicit val passageFormat = jsonFormat3(Passage)
    implicit val sensorFormat = jsonFormat2(Sensor)
    implicit val cellFormat = jsonFormat10(Cell)
    implicit val areaFormat = jsonFormat1(Area)
}

import admin.MyJsonProtocol._

class InterfaceController(interfaceView: InterfaceView) {
    var actorRef: ActorRef = _

    def newText(sampleUpdate: SampleUpdate): Unit = {
        Platform.runLater {
            interfaceView.setText1(sampleUpdate.temperature.toString)
            interfaceView.setText2(sampleUpdate.people.toString)
        }
    }

    def parseFile(file: File): Unit = {
        val source = Source.fromFile(file).getLines.mkString
        val jsvalue: JsValue = source.parseJson
        val area: Area = jsvalue.convertTo[Area]
        actorRef ! Message.FromInterface.ToAdmin.MAP_CONFIG(area)
    }

    def triggerAlarm() = {
        actorRef ! Message.FromInterface.ToAdmin.ALARM
    }

}
