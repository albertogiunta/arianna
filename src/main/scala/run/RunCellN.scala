package run

import java.io.File

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
    
        // "*root*/res/conf/test/testCell1.conf" "*root*/res/json/cell/cell1.json"
        val path2AkkaConfig = args(0)
        val pathToCellConfig = args(1)
        
        implicit val config: Config = ConfigFactory.parseFile(new File(path2AkkaConfig))
            .withFallback(ConfigFactory.load()).resolve()
        
        val system = ActorSystem(NamingSystem.ActorSystem, config)
        
        val middleware = DistributedPubSub(system).mediator
        
        val loadedConf = ConfigurationManager(system)
        val builder = ConfigPathBuilder()
    
        val serialNumber = loadedConf.property(builder.akka.actor.get("serial-number")).asString
        
        val core = system.actorOf(Props(new CellCoreActor(middleware)), NamingSystem.CellCore + serialNumber)
        
        core ! AriadneMessage(Init, Init.Subtype.Greetings,
            Location.Master >> Location.Self, Greetings(List(pathToCellConfig)))
        
    } else {
        println(s"Wrong number of Arguments... Wanted $REQUIRED_ARGS, found " + args.length)
        System.exit(0)
    }
    
}