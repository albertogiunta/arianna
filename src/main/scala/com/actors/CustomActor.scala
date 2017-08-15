package com.actors

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSelection, Stash}
import akka.extension._
import ontologies.messages.{AriadneMessage, Greetings, MessageType}

/**
  * A CustomActor that ease the use of few overused methods
  *
  * Created by Alessandro on 01/07/2017.
  */
trait CustomActor extends Actor with Stash with ActorLogging {

    protected val config = ConfigurationManager(context.system)
    protected val builder = ConfigPathBuilder()

    /**
      * A shortcut method that gives the name of this actor
      *
      * @return The name of this actor
      */
    def name: String = self.path.name

    /**
      * A shortcut method that gives the ArctorRef of the parent Actor of this Actor
      *
      * @return The parent ActorRef
      */
    def parent: ActorRef = this.context.parent

    /**
      * A shortcut method that gives the ActorSelection of the Sibling with the
      * specified name, if exist
      *
      * @param name The name of the sibling
      * @return Return an Option[ActorSelection] of the found sibling
      */
    def sibling(name: String): Option[ActorSelection] =
        Option(context.actorSelection(parent.path / name))

    /**
      * A shortcut method that gives all the siblings of this Actor
      *
      * @return Return all the siblings of this node including itself
      */
    def siblings: ActorSelection = context.actorSelection(parent.path / "*")

    /**
      * A shortcut method that gives the ActorSelection of the Child with the specified name
      *
      * @param name The name of the wanted child
      * @return An Option[ActorSelection] of the found child
      */
    def child(name: String): Option[ActorSelection] =
        Option(context.actorSelection(self.path / name))

    /**
      * A shortcut method that gives all the children of this Actor
      *
      * @return All the children of this Actor
      */
    def children: ActorSelection = context.actorSelection(self.path / "*")
    
}

/**
  * An actual implementation of CustomActor with the addition of few protected method
  * that define the base behaviour of such Actor.
  *
  * This kind of Actor is supposed to be used for Actors that need an Initialization
  * or need wait before becoming fully operative.
  *
  * <b>NOTE</b>: The concept used here is one of a <u>Template Pattern</u>, this is the provided template,
  * The implementation only provide the effective behaviour.
  *
  * Created by Alessandro on 01/07/2017.
  */
abstract class TemplateActor extends CustomActor {
    
    override def preStart = {
        log.info("Hello there, I need to be initialized!")
    }
    
    override def receive: Receive = resistive

    /**
      * The resistive method is the default behaviour of the BasicActor
      * where it waits for an InitMessage.
      *
      * Can be overrided if needed but remember to call this old behaviour at the end of the case match.
      *
      * Like this:
      * case 1 => Do stuff
      * case 2 => Do other stuff
      * ...
      * case msg => super.resistive(msg)
      *
      * @return An Actor Receive Behaviour
      */
    protected def resistive: Actor.Receive = {
        case AriadneMessage(MessageType.Init, _, _, content: Greetings) =>
            try {
                this.init(content.args)
                this.context.become(receptive, discardOld = true)
                log.info("I've become receptive!")
            } catch {
                case ex: Throwable => ex.printStackTrace()
            }
        case _ => desist _
    }

    /**
      * This is the default init for the BasicActor, it simply log a message of self existence.
      *
      * To override if particular Initialization is need, that is the List of arguments have to be used.
      *
      * @param args A List of Arguments need to initialize post creation this Actor.
      */
    protected def init(args: List[Any]): Unit = {
        log.info("Hello there from {}!", name)
    }

    /**
      * This method is the behaviour on which the BasicActor will be
      * after the Initialization has been completed.
      *
      * @return An Actor Receive Behaviour
      */
    protected def receptive: Actor.Receive

    /**
      * This method is the default action to be used for unhandled messages
      *
      * @param msg The unhandled message from the current behaviour
      */
    protected def desist(msg: Any): Unit = log.info("Unhandled... {}", msg.toString) // Ignore
}