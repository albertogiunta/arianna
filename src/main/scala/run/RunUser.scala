package run

import java.io.File
import java.nio.file.Paths

import akka.actor.{ActorRef, ActorSystem, Props}
import cell.core.UserManager
import com.typesafe.config.ConfigFactory
import ontologies.messages.Location._
import ontologies.messages.MessageType.Topology
import ontologies.messages.MessageType.Topology.Subtype.{Planimetrics, ViewedFromACell}
import ontologies.messages._

import scala.collection.mutable
import scala.io.Source

object RunUser {

    def readJson(filename: String): String = {
        Source.fromFile(filename).getLines.mkString
    }

    def loadArea(path: String): Area = {
        Planimetrics.unmarshal(readJson(path))
    }

    def areaForCell: AreaViewedFromACell = {
        AreaViewedFromACell(area)
    }

    var area: Area = _

    def main(args: Array[String]): Unit = {

        val path2Project = Paths.get("").toFile.getAbsolutePath
        val path2Config = path2Project + "/res/conf/akka/application.conf"
        val path2map: String = path2Project + "/res/json/map15_room.json"
        val config = ConfigFactory.parseFile(new File(path2Config))
        val system = ActorSystem.create("userSystem", config.getConfig("user"))

        area = loadArea(path2map)

        val greetings: mutable.LinkedHashMap[String, Greetings] = mutable.LinkedHashMap()
        val actors: mutable.MutableList[ActorRef] = mutable.MutableList()

        for (i <- 1 to 15) {
            greetings.put(s"user$i", Greetings(List(s"uri$i", (8080 + i).toString)))
        }

        greetings.foreach(u => {
            val userActor = system.actorOf(Props.create(classOf[UserManager]), u._1)
            userActor ! AriadneMessage(MessageType.Init, MessageType.Init.Subtype.Greetings, Location.User >> Location.Self, u._2)
            Thread.sleep(1000)
            userActor ! AriadneMessage(Topology, ViewedFromACell, Location.User >> Location.Self, areaForCell)
            Thread.sleep(500)
            actors += userActor
        })
    }
}