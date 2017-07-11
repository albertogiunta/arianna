package master.cluster

import akka.cluster.pubsub.DistributedPubSubMediator._
import common.BasicPublisher
import ontologies.Topic
import ontologies.messages.Location._
import ontologies.messages.MessageType._
import ontologies.messages._
/**
  * Created by Alessandro on 28/06/2017.
  */
class MasterPublisher extends BasicPublisher {
    
    override protected def init(args: List[Any]) = {
        log.info("Hello there from {}!", name)
    
        mediator ! Publish(Topic.HandShakes,
            AriadneRemoteMessage(
                Handshake,
                Handshake.Subtype.Cell2Master,
                Location.Cell >> Location.Server,
                Handshake.Subtype.Cell2Master.marshal(
                    InfoCell(14321, "uri", "PancoPillo",
                        Coordinates(Point(1, 1), Point(-1, -1), Point(-1, 1), Point(1, -1)),
                        Point(0, 0)
                    )
                )
            )
        )
    }

    override protected val receptive = {

        case msg@AriadneLocalMessage(Alarm, _, _, _) =>
            mediator ! Publish(Topic.Updates, Message.local2remote(msg))

        case msg@AriadneLocalMessage(Topology, _, _, _) =>
            mediator ! Publish(Topic.Topologies, Message.local2remote(msg))
        case _ => desist _
    }
}