package cell.core

import java.io.File
import java.nio.file.Paths

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class UserManagerTest extends TestKit(ActorSystem("CellSubscriberTest", UserManagerTest.config))
    with ImplicitSender with WordSpecLike with Matchers with BeforeAndAfterAll {

    "A UserManager actor" must {

    }

}

object UserManagerTest {
    val path2Project = Paths.get("").toFile.getAbsolutePath
    val path2Config = path2Project + "/res/conf/akka/application.conf"

    val config: Config = ConfigFactory.parseFile(new File(path2Config)).withFallback(ConfigFactory.load()).resolve()
}