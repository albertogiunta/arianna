package master

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import master.cluster.Subscriber
import ontologies.MyMessage

object TestMaster extends App {
    
    implicit val config = ConfigFactory.parseString(
        """
              akka {
              |  actor {
              |    provider = "akka.cluster.ClusterActorRefProvider"
              |  }
              |  remote {
              |    log-remote-lifecycle-events = off
              |    netty.tcp {
              |      hostname = "127.0.0.1"
              |      port = 0
              |    }
              |  }
              |  log-dead-letters = 10
              |  log-dead-letters-during-shutdown = no
              |  loglevel = "INFO"
              |  cluster {
              |    seed-nodes = [
              |      "akka.tcp://ClusterSystem@127.0.0.1:2551",
              |      "akka.tcp://ClusterSystem@127.0.0.1:2552"]
              |
              |    # auto downing is NOT safe for production deployments.
              |    # you may want to use it during development, read more about it in the docs.
              |    #
              |    # auto-down-unreachable-after = 10s
              |  }
              |}
              |
              |# Disable legacy metrics in akka-cluster.
              |akka.cluster.metrics.enabled = off
              |
              |# Enable metrics extension in akka-cluster-metrics.
              |akka.extensions = ["akka.cluster.metrics.ClusterMetricsExtension"]
              |
              |# Sigar native library extract location during tests.
              |# Note: use per-jvm-instance folder when running multiple jvm on one host.
              |akka.cluster.metrics.native-library-extract-folder=${user.dir}/target/native
            """.stripMargin
    ).withFallback(ConfigFactory.load()).resolve()
    
    implicit val system = ActorSystem("Arianna-Master-cluster", config)
    
    println("ActorSystem is now Active...")
    
    val actor = system.actorOf(Props[Subscriber], "Subscriber")
    
    actor ! MyMessage(ontologies.Init, null)
}
