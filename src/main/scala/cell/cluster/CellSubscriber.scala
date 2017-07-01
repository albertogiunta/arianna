package cell.cluster

import common.BasicSubscriber
import ontologies._

/**
  * An actor that models a Cell receiver for the Cells-MasterServer
  * Publish-Subscribe interaction model
  *
  * Created by Matteo Gabellini on 29/06/2017.
  */
class CellSubscriber extends BasicSubscriber {
    
    override val topics = Set(Topic.Alarm, Topic.Topology)
    
    override protected def init(args: Any): Unit = {
        log.info("Hello there from {}!", name)
    }
    
    override protected def receptive = {
        
        case msg@AriadneMessage(MessageType.Alarm, cnt) =>
            log.info("Got {} from {} of Type {}", cnt, sender.path.name, msg.messageType)
        case msg@AriadneMessage(MessageType.Topology, cnt) =>
            log.info("Got {} from {} of Type {}", cnt, sender.path.name, msg.messageType)
        case _ => desist _
    }
}

