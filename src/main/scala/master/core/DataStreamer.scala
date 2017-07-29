package master.core

import akka.actor.ActorSelection
import akka.stream._
import akka.stream.scaladsl.{Flow, Sink, Source, SourceQueueWithComplete}
import com.actors.BasicActor
import ontologies.messages.Location._
import ontologies.messages.MessageType.Update.Subtype
import ontologies.messages.MessageType.{Handshake, Update}
import ontologies.messages._
import system.names.NamingSystem

import scala.concurrent.duration._

/**
  * This actor filters updates to admin when they are made faster that @timeout sec from one another
  *
  * Created by Alessandro on 06/07/2017.
  */
class DataStreamer(private val handler: (AriadneMessage[_], ActorSelection) => Unit = (msg, target) => target ! msg)
    extends BasicActor {
    
    implicit private val system = context.system
    implicit private val executionContext = system.dispatcher
    implicit private val materializer: ActorMaterializer = ActorMaterializer.create(system)
    
    private var streamer: SourceQueueWithComplete[Iterable[Cell]] = _
    private val admin: () => ActorSelection = () => sibling(NamingSystem.AdminManager).get
    
    private val source = Source.queue[Iterable[Cell]](100, OverflowStrategy.dropHead)
    
    private val stream = Flow[Iterable[Cell]]
        .map(map => UpdateForAdmin(map.map(c => CellDataUpdate(c)).toList))
        .map(updates => AriadneMessage(Update, Subtype.UpdateForAdmin, Location.Master >> Location.Admin, updates))
        .throttle(1, 1000 milliseconds, 1, ThrottleMode.Shaping)
        .to(Sink.foreach(msg => handler(msg, admin())))
    
    override def preStart = {
        streamer = stream.async.runWith(source)
        super.preStart
    }
    
    override def postStop = {
        streamer.complete()
        super.postStop
    }
    
    override protected def receptive: Receive = {
    
        case msg: Iterable[Cell] =>
            streamer offer msg

        case msg@AriadneMessage(Handshake, Handshake.Subtype.CellToMaster, _, _) => admin() ! msg

        case msg => log.info(msg.toString)
    }
}

