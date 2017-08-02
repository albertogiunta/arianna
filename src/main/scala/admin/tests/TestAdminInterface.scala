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
        var sensors: ListBuffer[SensorInfo] = new ListBuffer[SensorInfo]
        for (i <- 1 until 5) {
            sensors += SensorInfo(i, (Math.random() * 10).round.toDouble)
        }
        var sensorList: SensorsInfoUpdate = SensorsInfoUpdate(CellInfo(i, "uri0", 0, "a", new Coordinates(Point(1, 1), Point(1, 1), Point(1, 1), Point(1, 1)), Point(1, 1)),
            sensors.toList)
        adminManager ! AriadneMessage(Handshake, Handshake.Subtype.CellToMaster, Location.Master >> Location.Admin, sensorList)
        i = i + 1
    }

    Thread.sleep(1000)

    while (true) {
        println("Invio aggiornamento... ")
        Thread.sleep(1000)
        var update: ListBuffer[RoomDataUpdate] = new ListBuffer[RoomDataUpdate]
        var sensors: ListBuffer[SensorInfo] = new ListBuffer[SensorInfo]
        for (i <- 1 until 5) {
            sensors += SensorInfo(i, (Math.random() * 10).round.toDouble)
        }

        for (i <- 1 until 6) {
            update += new RoomDataUpdate(new CellInfo(i, "uri0", 0, "a", new Coordinates(Point(1, 1), Point(1, 1), Point(1, 1), Point(1, 1)), Point(1, 1)), (Math.random() * 50).round.toInt, sensors.toList)
        }
        adminManager ! AriadneMessage(MessageType.Update, MessageType.Update.Subtype.Admin, Location.Master >> Location.Admin,
            new AdminUpdate(update.toList))
    }
}

