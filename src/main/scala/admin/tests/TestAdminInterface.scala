package admin.tests

import java.io.File
import java.nio.file.Paths

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import master.core.AdminManager
import ontologies.messages.Location._
import ontologies.messages.MessageType.Handshake
import ontologies.messages._

import scala.collection.mutable.ListBuffer

object TestAdminInterface extends App {

    val path2Project = Paths.get("").toFile.getAbsolutePath
    val path2Config = path2Project + "/res/conf/akka/master.conf"
    //val path2Config2 = path2Project + "/res/conf/akka/application.conf"
    val config = ConfigFactory.parseFile(new File(path2Config))
        .withFallback(ConfigFactory.load()).resolve()

    val system = ActorSystem.create("Arianna-Cluster", config)

    val adminManager = system.actorOf(Props[AdminManager], "AdminManager")
    var i = 1
    Thread.sleep(5000)
    while (!i.equals(6)) {
        Thread.sleep(1000)
        var sensors: ListBuffer[Sensor] = new ListBuffer[Sensor]
        for (i <- 1 until 4) {
            sensors += Sensor(i, (Math.random() * 10).round.toDouble, 0, 10)
        }
        println(sensors.toString)
        var sensorList: SensorList = SensorList(InfoCell(i, "uri0", "a", new Coordinates(Point(1, 1), Point(1, 1), Point(1, 1), Point(1, 1)), Point(1, 1)), sensors.toList)
        adminManager ! AriadneMessage(Handshake, Handshake.Subtype.Cell2Master, Location.Server >> Location.Admin, sensorList)
        println(i.toString)
        i = i + 1
    }

}

