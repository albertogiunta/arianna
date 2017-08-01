package admin.tests

import java.io.File
import java.nio.file.Paths

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import master.core.AdminManager
import ontologies.messages.Location._
import ontologies.messages.MessageType.Alarm
import ontologies.messages._

object TestAlarm extends App {

    val path2Project = Paths.get("").toFile.getAbsolutePath
    val path2Config = path2Project + "/res/conf/akka/master.conf"
    //val path2Config2 = path2Project + "/res/conf/akka/application.conf"
    val config = ConfigFactory.parseFile(new File(path2Config))
        .withFallback(ConfigFactory.load()).resolve()

    val system = ActorSystem.create("Arianna-Cluster", config)

    val adminManager = system.actorOf(Props[AdminManager], "AdminManager")
    Thread.sleep(10000)
    val alarmContent = AlarmContent(InfoCell(2, "uri2", "cell2", new Coordinates(Point(1, 1), Point(1, 1), Point(1, 1), Point(1, 1)), Point(1, 1)), false, false)
    adminManager ! AriadneMessage(Alarm, Alarm.Subtype.Basic, Location.Master >> Location.Admin, alarmContent)

}
