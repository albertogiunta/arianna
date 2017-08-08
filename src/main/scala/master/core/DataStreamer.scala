package master.core

import akka.actor.ActorSelection
import akka.stream._
import akka.stream.scaladsl.{Flow, Sink, Source, SourceQueueWithComplete}
import com.actors.CustomActor
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
class DataStreamer(private val target: ActorSelection,
                   private val handler: (AriadneMessage[_], ActorSelection) => Unit = (msg, dest) => dest ! msg)
    extends CustomActor {
    
    implicit private val system = context.system
    implicit private val executionContext = system.dispatcher
    implicit private val materializer: ActorMaterializer = ActorMaterializer.create(system)
    
    private var streamer: SourceQueueWithComplete[Iterable[Room]] = _
    
    private val source = Source.queue[Iterable[Room]](100, OverflowStrategy.dropHead)
    
    private val stream = Flow[Iterable[Room]]
        .map(map => AdminUpdate(0, map.map(c => RoomDataUpdate(c)).toList))
        .map(updates => AriadneMessage(Update, Subtype.Admin, Location.Master >> Location.Admin, updates))
        .throttle(1, 1000 milliseconds, 1, ThrottleMode.Shaping)
        .to(Sink.foreach(msg => handler(msg, target)))
    
    override def preStart = {
        streamer = stream.async.runWith(source)
        super.preStart
    }
    
    override def postStop = {
        streamer.complete()
        super.postStop
    }
    
    override def receive: Receive = {
        
        case msg: Iterable[Room] =>
            streamer offer msg
        
        case msg => log.info(msg.toString)
    }
}

