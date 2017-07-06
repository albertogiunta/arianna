package common

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSelection, Stash}
import akka.extension._
import ontologies.messages.{AriadneLocalMessage, Greetings, MessageType}

/**
  * A CustomActor that ease the use of few overkilled methods
  *
  * Created by Alessandro on 01/07/2017.
  */
abstract class CustomActor extends Actor with Stash with ActorLogging {

    protected val config = ConfigurationManager(context.system)
    protected val builder = ConfigPathBuilder()

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
        case AriadneLocalMessage(MessageType.Init, _, _, content: Greetings) =>
            
            try {
                this.init(content.args)
            } catch {
                case ex: Throwable => ex.printStackTrace()
            }
    
            this.context.become(receptive, discardOld = true)
            log.info("[{}] I've become receptive!", name)

        case _ => desist _
    }
    
    protected def init(args: List[Any])
    
    protected def receptive: Actor.Receive
    
    protected def desist(msg: Any): Unit = log.info("Unhandled message... {}", msg.toString) // Ignore
}