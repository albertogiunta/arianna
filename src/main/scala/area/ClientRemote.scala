package area

import java.io.File

import akka.actor.{Actor, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

/**
  * Created by lisamazzini on 29/06/17.
  */
class ClientRemote extends Actor{
  def receive = {
    case msg : String => msg match {
      case "Start" => {
        println("Sending to server...")
        context.actorSelection("akka.tcp://serverSystem@127.0.0.1:4552/user/server").tell("Hello!", self)
      }
      case "Received, hello!" => print("Received answer from server")
      case "Alarm!" => {
        println("Alarm!")
      }
    }
  }

}

object ClientM {
  def main(args: Array[String]): Unit = {
    val config = ConfigFactory.parseFile(new File("src/main/scala/area/client.conf"))
    val system = ActorSystem.create("clientSystem", config)
    var client = system.actorOf(Props.create(classOf[ClientRemote]), "client")
    client ! "Start"
  }
}