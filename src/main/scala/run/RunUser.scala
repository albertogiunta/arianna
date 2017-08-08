package run

import java.io.File
import java.nio.file.Paths

import akka.actor.{ActorRef, ActorSystem, Props}
import cell.core.UserManager
import com.typesafe.config.ConfigFactory
import ontologies.messages.Location._
import ontologies.messages.MessageType.Topology
import ontologies.messages.MessageType.Topology.Subtype.ViewedFromACell
import ontologies.messages._
import spray.json._

import scala.collection.mutable
import scala.io.Source

object RunUser {

    private def readJson(filename: String): JsValue = {
        val source: String = Source.fromFile(filename).getLines.mkString
        source.parseJson
    }

    def loadArea(): Area = {
        val area = readJson(s"res/json/map15_room.json").convertTo[Area]
        area
    }

    def areaForCell: AreaViewedFromACell = {
        AreaViewedFromACell(area)
    }

    var area: Area = loadArea()

    def main(args: Array[String]): Unit = {

        val path2Project = Paths.get("").toFile.getAbsolutePath
        val path2Config = path2Project + "/res/conf/akka/application.conf"
        val config = ConfigFactory.parseFile(new File(path2Config))
        val system = ActorSystem.create("userSystem", config.getConfig("user"))

        val map: mutable.LinkedHashMap[String, Greetings] = mutable.LinkedHashMap()
        val actors: mutable.MutableList[ActorRef] = mutable.MutableList()

        for (i <- 1 to 15) {
            map.put(s"user$i", Greetings(List(s"uri$i", (8080 + i).toString)))
        }

        map.foreach(u => {
            val userActor = system.actorOf(Props.create(classOf[UserManager]), u._1)
            userActor ! AriadneMessage(MessageType.Init, MessageType.Init.Subtype.Greetings, Location.User >> Location.Self, u._2)
            Thread.sleep(1000)
            userActor ! AriadneMessage(Topology, ViewedFromACell, Location.User >> Location.Self, areaForCell)
            Thread.sleep(500)
            actors += userActor
        })
    }
}