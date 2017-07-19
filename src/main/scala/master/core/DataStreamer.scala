package master.core

import akka.actor.ActorSelection
import akka.stream._
import akka.stream.scaladsl.{Flow, Sink, Source, SourceQueueWithComplete}
import common.CustomActor
import ontologies.messages.Location._
import ontologies.messages.MessageType.Update
import ontologies.messages.MessageType.Update.Subtype
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
    
    private var streamer: SourceQueueWithComplete[Iterable[Cell]] = _
    private var admin: ActorSelection = _
    
    private val handler: AriadneMessage[_] => Unit = msg => admin ! msg //println(Thread.currentThread().getName + " - " + msg)
    
    private val source = Source.queue[Iterable[Cell]](100, OverflowStrategy.dropHead)
    
    private val stream = Flow[Iterable[Cell]]
        .map(map => UpdateForAdmin(map.map(c => CellUpdate(c)).toList))
        .map(updates => AriadneMessage(Update, Subtype.UpdateForAdmin, Location.Server >> Location.Admin, updates))
        .throttle(1, 1000 milliseconds, 1, ThrottleMode.Shaping)
        .to(Sink.foreach(msg => handler(msg)))
    
    override def preStart = {
        admin = sibling("AdminManager").get
        println(admin)
        streamer = stream.async.runWith(source)
        super.preStart
    }
    
    override def postStop = {
        streamer.complete()
        super.postStop
    }
    
    override def receive: Receive = {
    
        case msg: Iterable[Cell] =>
            streamer offer msg
        case _ =>
    }
}

