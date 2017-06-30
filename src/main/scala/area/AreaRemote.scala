package area

import java.io.File

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

/**
  * Created by lisamazzini on 29/06/17.
  */
class AreaRemote extends Actor{

  var from : ActorRef = null
  var established : Boolean = false

  def receive = {
    case msg : String => msg match {
      case "Hello!" => {
        from = sender()
        established = true
        sender().tell("Received, hello!", self)
      }
      case "sendAlarm" => {
        if(established) {
          from.tell("Alarm!", self)
        }
      }

    }
  }

}

object ServerM {
  def main(args: Array[String]): Unit = {
    val config = ConfigFactory.parseFile(new File("src/main/scala/area/server.conf"))
    val system = ActorSystem.create("serverSystem", config)
    var area = system.actorOf(Props.create(classOf[AreaRemote]), "server")
    Thread.sleep(10000)
    area.tell("sendAlarm", null)
  }
}
