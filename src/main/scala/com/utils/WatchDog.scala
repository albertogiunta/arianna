package com.utils

import akka.actor.ActorRef
import com.utils.WatchDog.WatchDogNotification

/**
  * Trait for a generic watch dog timer
  * Created by Matteo Gabellini on 10/08/2017.
  */
trait WatchDog {
    
    /**
      * Notify to the watch dog that the expected
      * event occurred
      **/
    def notifyEventOccurred: Unit
}

object WatchDog {
    val waitTime = 5000
    
    /**
      * Generic notification sent from a watch dog to the relative actor
      **/
    case class WatchDogNotification(content: Any = "Time exceeded")
}

/**
  * Basic implementation of a watch dog timer
  *
  * @param actorToNotifyTimeOut the actor that will be notified when the time exceed
  * @param waitTime             the time value after which the actor will be notified,
  *                             the default value is the waitTime value specified in the WatchDog companion object
  **/
class BasicWatchDog(actorToNotifyTimeOut: ActorRef, waitTime: Long = WatchDog.waitTime) extends Thread with WatchDog {
    
    var eventOccurred: Boolean = false

    override def run(): Unit = {
        super.run()
        Thread.sleep(waitTime)
        if (!eventOccurred) actorToNotifyTimeOut ! WatchDogNotification
    }
    
    override def notifyEventOccurred: Unit = eventOccurred = true
}

/**
  *
  * @param actorToNotifyTimeOut the actor that will be notified when the time exceed
  * @param hookedCell           the cell to which this WatchDog is associated
  * @param waitTime             the time value after which the actor will be notified,
  *                             the default value is the waitTime value specified in the WatchDog companion object
  */
class CellWatchDog(actorToNotifyTimeOut: ActorRef,
                   hookedCell: String,
                   waitTime: Long = WatchDog.waitTime) extends Thread with WatchDog {
    
    var eventOccurred: Boolean = false
    
    override def run(): Unit = {
        super.run()
        Thread.sleep(waitTime)
        if (!eventOccurred) actorToNotifyTimeOut ! WatchDogNotification(hookedCell)
    }
    
    override def notifyEventOccurred: Unit = eventOccurred = true
}