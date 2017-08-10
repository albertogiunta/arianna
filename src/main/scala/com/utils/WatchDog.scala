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
    def notofyEventHappened: Unit
}

object WatchDog {
    val waitTime = 5000

    /**
      * Generic notification sent from a watch dog to the relative actor
      **/
    case class WatchDogNotification() {
        val content: Any = "Time exceeded"
    }
}

/**
  * Basic implementation of a watch dog timer
  *
  * @param actorToNotifyTimeOut the actor that will be notified when the time exceed
  * @param waitTime             the time value after which the actor will be notified,
  *                             the default value is the waitTime value specified in the WatchDog companion object
  **/
class BasicWatchDog(actorToNotifyTimeOut: ActorRef, waitTime: Long = WatchDog.waitTime) extends Thread with WatchDog {
    var eventHappened: Boolean = false

    override def run(): Unit = {
        super.run()
        Thread.sleep(waitTime)
        if (!eventHappened) actorToNotifyTimeOut ! WatchDogNotification
    }

    override def notofyEventHappened: Unit = eventHappened = true
}