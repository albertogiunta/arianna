package com.utils

import akka.actor.ActorRef
import com.utils.WatchDog.WatchDogNotification

/**
  * Created by Matteo Gabellini on 10/08/2017.
  */
trait WatchDog {
    def notofyEventHappened: Unit
}

object WatchDog {
    val waitTime = 5000

    case class WatchDogNotification() {
        val content: String = "Time exceeded"
    }

}

class BasicWatchDog(actorToNotifyTimeOut: ActorRef, waitTime: Long = WatchDog.waitTime) extends Thread with WatchDog {
    var eventHappened: Boolean = false

    override def run(): Unit = {
        super.run()
        Thread.sleep(waitTime)
        if (!eventHappened) actorToNotifyTimeOut ! WatchDogNotification
    }

    override def notofyEventHappened: Unit = eventHappened = true
}