package run

import java.io.File
import java.nio.file.Paths

import akka.actor.{ActorSystem, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.extension.{ConfigPathBuilder, ConfigurationManager}
import cell.core.CellCoreActor
import com.typesafe.config.{Config, ConfigFactory}
import ontologies.messages.Location._
import ontologies.messages.MessageType.Init
import ontologies.messages.{AriadneMessage, Greetings, Location}
import system.names.NamingSystem

/**
  * Created by Alessandro on 28/06/2017.
  */
object RunCellN extends App {
    
    val REQUIRED_ARGS = 2
    
    if (args.length == REQUIRED_ARGS) {
        
        val path2Project: String = Paths.get("").toFile.getAbsolutePath + "/"
        
        if (args(0).startsWith("/")) args(0).replaceFirst("/", "")
        if (args(1).startsWith("/")) args(1).replaceFirst("/", "")
        
        val path2AkkaConfig = if (!args(0).contains(path2Project)) path2Project + args(0) else args(0)
        
        val pathToCellConfig = if (!args(1).contains(path2Project)) path2Project + args(1) else args(1)
        
        // "/res/conf/test/testCell1.conf" "/res/json/cell/cell1.json"
        
        implicit val config: Config = ConfigFactory.parseFile(new File(path2AkkaConfig))
            .withFallback(ConfigFactory.load()).resolve()
        
        val system = ActorSystem(NamingSystem.ActorSystem, config)
        
        val middleware = DistributedPubSub(system).mediator
        
        val loadedConf = ConfigurationManager(system)
        val builder = ConfigPathBuilder()
        
        val serialNumber = loadedConf.property(builder.akka.actor.get("serial-number")).string
        
        val core = system.actorOf(Props(new CellCoreActor(middleware)), NamingSystem.CellCore + serialNumber)
        
        core ! AriadneMessage(Init, Init.Subtype.Greetings,
            Location.Master >> Location.Self, Greetings(List(pathToCellConfig)))
        
    } else {
        println(s"Wrong number of Arguments... Wanted $REQUIRED_ARGS, found " + args.length)
        System.exit(0)
    }
    
}