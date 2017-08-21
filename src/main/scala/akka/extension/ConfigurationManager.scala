package akka.extension

import java.io.File
import java.nio.file.Paths

import akka.actor.{ActorSystem, ExtendedActorSystem, Extension, ExtensionId, ExtensionIdProvider}
import com.typesafe.config.{Config, ConfigFactory, ConfigObject, ConfigValue}

import scala.collection.JavaConverters.asScalaBuffer

/**
  * Created by Alessandro on 02/07/2017.
  */
class ConfigurationManagerImpl(system: ActorSystem) extends Extension {

    def config: Config = system.settings.config

    def config(path: String): Config = config.getConfig(path)

    def property(path: String): PropertyChooser = PropertyChooser(config, path)
}

final case class PropertyChooser(config: Config, path: String) {
    
    def asString: String = config.getString(path)
    
    def asStringList: List[String] =
        asScalaBuffer(config.getStringList(path)).toList
    
    def asNumber: Number = config.getNumber(path)
    
    def asNumberList: List[Number] = asScalaBuffer(config.getNumberList(path)).toList
    
    def asConfigValue: ConfigValue = config.getValue(path)
    
    def asConfigObject: ConfigObject = config.getObject(path)
    
    def asConfigObjList: List[ConfigObject] = asScalaBuffer(config.getObjectList(path)).toList
}

case class ConfigPathBuilder() {
    
    private var path: List[String] = List.empty[String]

    def akka: ConfigPathBuilder = {
        path = "akka" :: path
        this
    }

    def actor: ConfigPathBuilder = {
        path = "actor" :: path
        this
    }

    def remote: ConfigPathBuilder = {
        path = "remote" :: path
        this
    }

    def cluster: ConfigPathBuilder = {
        path = "cluster" :: path
        this
    }

    def netty: ConfigPathBuilder = {
        path = "netty" :: path
        this
    }

    def tcp: ConfigPathBuilder = {
        path = "tcp" :: path
        this
    }

    def extensions: ConfigPathBuilder = {
        path = "extensions" :: path
        this
    }

    def get(prop: String): String = (prop :: path).reverse.mkString(".")
}

object ConfigurationManager extends ExtensionId[ConfigurationManagerImpl] with ExtensionIdProvider {
    
    override def lookup: ConfigurationManager.type = ConfigurationManager

    override def createExtension(system: ExtendedActorSystem) =
        new ConfigurationManagerImpl(system)

    /**
      * Java API: retrieve the Settings extension for the given system.
      */
    override def get(system: ActorSystem): ConfigurationManagerImpl = super.get(system)
}

object TryConfigManager extends App {
    val path2Project = Paths.get("").toFile.getAbsolutePath
    val path2Config = path2Project + "/res/conf/akka/master.conf"
    
    implicit val config: Config = ConfigFactory.parseFile(new File(path2Config))
        .withFallback(ConfigFactory.load()).resolve()
    
    implicit val system: ActorSystem = ActorSystem("Arianna-Cluster", config)
    
    println(ConfigurationManager(system) property ConfigPathBuilder().akka.remote.netty.tcp.get("port") asNumber)

}