package master.cluster

import akka.actor.{ActorSelection, ActorSystem, Props}
import akka.stream._
import akka.stream.scaladsl.{Flow, Sink, Source, SourceQueueWithComplete}
import common.CustomActor
import ontologies.messages.Location._
import ontologies.messages.MessageType.Update
import ontologies.messages.MessageType.Update.Subtype.Sensors
import ontologies.messages._

import scala.concurrent.duration._
import scala.util.Random

/**
  * This actor filters updates to admin when they are made faster that @timeout sec from one another
  *
  * Created by Alessandro on 06/07/2017.
  */
class DataStreamer extends CustomActor {
    
    implicit private val system = context.system
    implicit private val executionContext = system.dispatcher
    implicit private val materializer: ActorMaterializer = ActorMaterializer.create(system)
    
    private val messageHandler: AriadneLocalMessage[_] => Unit = msg => println(Thread.currentThread().getName + " - " + msg.content)
    
    private val dataSource = Source.queue[AriadneLocalMessage[_]](1000, OverflowStrategy.backpressure)
    
    private val dataStream = Flow[AriadneLocalMessage[_]]
        .throttle(1, 1000 milliseconds, 5, ThrottleMode.Shaping)
        .to(Sink.foreach(msg => messageHandler(msg)))
    
    private var streamer: SourceQueueWithComplete[AriadneLocalMessage[_]] = _
    private var admin: ActorSelection = _
    
    override def preStart = {
        admin = sibling("AdminManager").get
        streamer = dataStream.async.runWith(dataSource)
        super.preStart
    }
    
    override def postStop = {
        streamer.complete()
        super.postStop
    }
    
    override def receive: Receive = {
        
        case msg@AriadneLocalMessage(Update, Sensors, _, _: SensorList) =>
            log.info(Thread.currentThread().getName + " - Streaming to handler...")
            streamer offer msg
        case _ =>
    }
}

object TestDataStreamer extends App {
    
    val toJsonObj: String => SensorList = s => Sensors.unmarshal(s)
    
    implicit val system = ActorSystem("Test")
    
    val streamer = system.actorOf(Props[DataStreamer], "DataStreamer")
    
    (0 to Int.MaxValue).foreach(_ => {
        val jsonStr: String = Sensors
            .marshal(
                SensorList(
                    InfoCell(Random.nextInt, "uri" + Random.nextInt, "name" + Random.nextInt,
                        Coordinates(Point(1, 1), Point(-1, -1), Point(-1, 1), Point(1, -1)),
                        Point(0, 0)
                    ),
                    List(Sensor(1, 2.0), Sensor(2, 1.55))
                )
            )
        Thread.sleep(100)
        streamer ! AriadneLocalMessage(
            Update,
            Sensors,
            Location.Cell >> Location.Server,
            toJsonObj(jsonStr)
        )
    })
}