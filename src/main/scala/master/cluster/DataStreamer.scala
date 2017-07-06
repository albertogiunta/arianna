package master.cluster

import akka.actor.{ActorSelection, ActorSystem, Cancellable, Props}
import akka.stream._
import akka.stream.scaladsl.{Sink, Source}
import common.CustomActor
import ontologies.messages.Location._
import ontologies.messages.MessageType.Update
import ontologies.messages.MessageType.Update.Subtype.Sensors
import ontologies.messages._

import scala.collection.mutable
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
    implicit private val materializer: ActorMaterializer = ActorMaterializer()
    
    private val queue: mutable.Queue[AriadneLocalMessage[SensorList]] = mutable.Queue.empty
    private val messageHandler: AriadneLocalMessage[SensorList] => Unit = msg => println(msg.content.info)
    
    private val timeout = 500 millisecond
    private val delay = 1 seconds
    
    private val time =
        Source.tick(delay, timeout, messageHandler)
            .to(Sink.foreach(t => {
                if (queue.nonEmpty) {
                    t(queue.dequeue)
                }
            }))
    
    //    private val data: Source[Message4Admin, SourceQueueWithComplete[Message4Admin]] =
    //        Source.queue[Message4Admin](1000, OverflowStrategy.backpressure)
    //
    //    private val dataFlow: SourceQueueWithComplete[Message4Admin] =
    //        Flow[Message4Admin].to(Sink.foreach(e => println(e.content.list))).runWith(data)
    
    private var cancel: Cancellable = _
    private var admin: ActorSelection = _
    
    override def preStart = {
        admin = sibling("AdminManager").get
        cancel = time.async.run()
        super.preStart
    }
    
    override def postStop = {
        cancel.cancel
        super.postStop
    }
    
    override def receive: Receive = {
        
        case msg@AriadneLocalMessage(Update, Sensors, _, _: SensorList) =>
            //            println("Saving for Later... " + msg.supertype)
            queue += msg.asInstanceOf[AriadneLocalMessage[SensorList]]
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