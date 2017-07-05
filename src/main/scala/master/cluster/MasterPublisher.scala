package master.cluster

import akka.cluster.pubsub.DistributedPubSubMediator._
import common.BasicPublisher
import ontologies.Topic
import ontologies.messages.MessageType.Topology.Subtype.{Topology4Cell, Topology4CellLight}
import ontologies.messages.MessageType._
import ontologies.messages._

/**
  * Created by Alessandro on 28/06/2017.
  */
class MasterPublisher extends BasicPublisher {
    
    override protected def init(args: List[Any]) = {
        log.info("Hello there from {}!", name)
    }

    override protected val receptive = {
    
        case AriadneLocalMessage(Alarm, _, dir, cnt: AlarmContent) =>
        
            mediator ! Publish(Topic.Update,
                AriadneRemoteMessage(
                    Alarm,
                    Alarm.Subtype.Basic,
                    dir,
                    Alarm.Subtype.Basic.marshal(cnt)
                )
            )
    
        case msg@AriadneLocalMessage(Topology, Topology4Cell, _, cnt: AreaForCell) =>
            mediator ! Publish(
                Topic.Topology,
                AriadneRemoteMessage(
                    msg.supertype,
                    msg.subtype,
                    msg.direction,
                    Topology4Cell.marshal(cnt)
                )
            )
        case msg@AriadneLocalMessage(Topology, Topology4CellLight, _, cnt: LightArea) =>
            mediator ! Publish(
                Topic.Topology,
                AriadneRemoteMessage(
                    msg.supertype,
                    msg.subtype,
                    msg.direction,
                    Topology4CellLight.marshal(cnt)
                )
            )
        case _ => desist _
    }
}
