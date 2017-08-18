package admin.tests

import java.io.File
import java.nio.file.Paths

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import master.core.AdminSupervisor
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

    val adminManager = system.actorOf(Props[AdminSupervisor], "AdminManager")

    var i = 1
    Thread.sleep(10000)
    while (!i.equals(5)) {
        Thread.sleep(1000)
        var sensors: ListBuffer[SensorInfo] = new ListBuffer[SensorInfo]
        for (i <- 1 until 5) {
            sensors += SensorInfo(i, (Math.random * 10).round.toDouble)
        }
        var sensorList: SensorsInfoUpdate = SensorsInfoUpdate(CellInfo("uri" + i.toString, 8080 + i),
            sensors.toList)
        adminManager ! AriadneMessage(Handshake, Handshake.Subtype.CellToMaster, Location.Master >> Location.Admin, sensorList)
        i = i + 1
    }

    Thread.sleep(1000)
    val roomNames: ListBuffer[String] = new ListBuffer[String]()
    roomNames += "Room A"
    roomNames += "Room B"
    roomNames += "Room C"
    roomNames += "Room D"
    roomNames += "Room E"
    var iter: Iterator[String] = roomNames.iterator

    while (true) {
        Thread.sleep(1000)
        var update: ListBuffer[RoomDataUpdate] = new ListBuffer[RoomDataUpdate]
        var sensors: ListBuffer[SensorInfo] = new ListBuffer[SensorInfo]
        for (i <- 1 until 5) {
            sensors += SensorInfo(i, (Math.random * 10).round.toDouble)
        }

        for (i <- 1 until 6) {
            var id = RoomID(i, iter.next)
            if (iter.hasNext) {
                update += new RoomDataUpdate(id, ontologies.messages.Cell(CellInfo("uri" + i.toString, 8080 + i), sensors.toList), (Math.random * 50).toInt)
            } else {
                iter = roomNames.iterator
            }
        }
        adminManager ! AriadneMessage(MessageType.Update, MessageType.Update.Subtype.Admin, Location.Master >> Location.Admin,
            new AdminUpdate(1, update.toList))
    }
}

