package common

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSelection, Stash}
import ontologies._

/**
  * A CustomActor Rich Interface that ease the use of few overkilled methods
  *
  * Created by Alessandro on 01/07/2017.
  */
trait CustomActor extends Actor with Stash with ActorLogging {
    
    def name: String = self.path.name
    
    def parent: ActorRef = this.context.parent
    
    def sibling(name: String): Option[ActorSelection] =
        Option(context.actorSelection("../" + name))
    
    /**
      * @return Return all the siblings of this node including itself
      */
    def siblings: ActorSelection = context.actorSelection("../*")
    
    def child(name: String): Option[ActorSelection] =
        Option(context.actorSelection("../" + this.name + "/" + name))
    
    def children: ActorSelection = context.actorSelection("../" + name + "/*")
    
}

/**
  * An actual implementation of CustomActor with the addition of few protected method
  * that define the base behaviour of such Actor.
  *
  * Created by Alessandro on 01/07/2017.
  */
abstract class BasicActor extends CustomActor {
    
    override def receive = resistive
    
    protected def resistive: Actor.Receive = {
        case AriadneMessage(MessageType.Init, content) =>
            try {
                this.init(content)
            } catch {
                case ex: Throwable => ex.printStackTrace()
            } finally {
                this.context.become(receptive, discardOld = true)
                log.info("[{}] I've become receptive!", name)
            }
        
        case _ => desist _
    }
    
    protected def init(args: Any): Unit
    
    protected def receptive: Actor.Receive
    
    protected def desist(msg: Any): Unit =
        log.info("Unhandled message... {}", msg.toString) // Ignore
}