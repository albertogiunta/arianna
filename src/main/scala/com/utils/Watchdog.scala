package com.utils

import akka.actor.ActorRef
import com.utils.Watchdog.WatchDogNotification

/**
  * Trait for a generic watch dog timer
  * Created by Matteo Gabellini on 10/08/2017.
  */
trait Watchdog {
    
    /**
      * Notify to the watch dog that the expected
      * event occurred
      **/
    def notifyEventOccurred: Unit
}

object Watchdog {
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
class BasicWatchdog(actorToNotifyTimeOut: ActorRef, waitTime: Long = Watchdog.waitTime) extends Thread with Watchdog {
    
    var eventOccurred: Boolean = false

    override def run(): Unit = {
        super.run()
        Thread.sleep(waitTime)
        if (!eventOccurred) actorToNotifyTimeOut ! WatchDogNotification
    }
    
    override def notifyEventOccurred: Unit = eventOccurred = true
}