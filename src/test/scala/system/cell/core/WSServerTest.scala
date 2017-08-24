package system.cell.core

import io.vertx.core.Vertx
import org.scalatest.{FlatSpec, Matchers}
import system.cell.userManagement.{WSClient, WSServer}

class WSServerTest extends FlatSpec with Matchers {

    var vertx: Vertx = Vertx.vertx()
    var s: WSServer = new WSServer(vertx, null, "/uri1", 8081)
    var c: WSClient = new WSClient(vertx)

    new {
        vertx.deployVerticle(s)
        Thread.sleep(3000)
        vertx.deployVerticle(c)
        Thread.sleep(1000)
    }

    "The server" should "receive a new connection from a new user" in {
        c.sendMessageFirstConnection()
        Thread.sleep(500)
        s.usersWaitingForArea.size should be(1)
        s.sendAreaToNewUser("")
        s.usersWaitingForArea.size should be(0)
        c.sendMessageFirstConnection()
        Thread.sleep(500)
        s.usersWaitingForArea.size should be(1)
    }

    "The server" should "receive a new connection from an old user" in {
        c.sendMessageNormalConnection()
        Thread.sleep(500)
        s.usersWaitingForConnectionAck.size should be(1)
        s.sendAckToNewUser("")
        s.usersWaitingForConnectionAck.size should be(0)
        c.sendMessageNormalConnection()
        Thread.sleep(500)
        s.usersWaitingForConnectionAck.size should be(1)
    }


    "The server" should "receive a route request from a user" in {
        c.sendMessageAskRoute()
        Thread.sleep(500)
        s.usersWaitingForRoute.size should be(1)
        s.sendRouteToUsers(1, 2, "")
        s.usersWaitingForRoute.size should be(0)
        c.sendMessageAskRoute()
        Thread.sleep(500)
    }

    "The server" should "receive a new disconnection request from a user" in {
        c.sendMessageDisconnect()
        Thread.sleep(500)
        s.usersWaitingForConnectionAck.size should be(0)
        s.usersWaitingForArea.size should be(0)
        s.usersWaitingForDisconnection.size should be(0)
    }
}
