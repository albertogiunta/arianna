package master.cluster

import akka.actor.ActorSelection
import akka.stream._
import akka.stream.scaladsl.{Flow, Sink, Source, SourceQueueWithComplete}
import common.CustomActor
import ontologies.messages.Location._
import ontologies.messages.MessageType.Update
import ontologies.messages.MessageType.Update.Subtype.AdminUpdate
import ontologies.messages._

import scala.concurrent.duration._

/**
  * This actor filters updates to admin when they are made faster that @timeout sec from one another
  *
  * Created by Alessandro on 06/07/2017.
  */
class DataStreamer extends CustomActor {
    
    implicit private val system = context.system
    implicit private val executionContext = system.dispatcher
    implicit private val materializer: ActorMaterializer = ActorMaterializer.create(system)
    
    private val handler: AriadneLocalMessage[_] => Unit = msg => println(Thread.currentThread().getName + " - " + msg)
    
    private val source = Source.queue[Iterable[Cell]](1000, OverflowStrategy.backpressure)
    
    private val stream = Flow[Iterable[Cell]]
        .map(map => UpdateForAdmin(map.map(c => CellUpdate(c)).toList))
        .map(updates => AriadneLocalMessage(Update, AdminUpdate, Location.Server >> Location.Admin, updates))
        .throttle(1, 1000 milliseconds, 1, ThrottleMode.Shaping)
        .to(Sink.foreach(msg => handler(msg)))
    
    private var streamer: SourceQueueWithComplete[Iterable[Cell]] = _
    private var admin: ActorSelection = _
    
    override def preStart = {
        admin = sibling("AdminManager").get
        streamer = stream.async.runWith(source)
        super.preStart
    }
    
    override def postStop = {
        streamer.complete()
        super.postStop
    }
    
    override def receive: Receive = {
    
        case msg: Iterable[Cell] =>
            //            log.info(Thread.currentThread().getName + " - Streaming to handler...")
            streamer offer msg
        case _ =>
    }
}

