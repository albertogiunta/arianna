package master.cluster

import common.BasicActor

/**
  * Created by Alessandro on 05/07/2017.
  */
class UpdaterActor extends BasicActor {
    
    override protected def init(args: List[Any]): Unit = {
        log.info("Hello there from {}!", name)
    }
    
    override protected def receptive: Receive = {
        
        case _ => desist _
    }
}
