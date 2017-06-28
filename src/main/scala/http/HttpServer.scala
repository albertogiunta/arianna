package http

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory

import scala.concurrent.Future

/**
  * A simple Server trait which defines both Future oriented and Route/Stream oriented approach
  */
trait HttpServer {

     def bindAsync(hostname: String, port: Int): Future[Http.ServerBinding]

     def bindBlocking(hostname: String, port: Int): Future[Http.ServerBinding]

     def terminate(): Unit
}

/**
  * A simple Akka-Http server with Asynchronous Handler and Unblocking Dispatcher
  *
  * The implementation is based on the code provided by the Akka Documentation
  * and this gitHub Repo @link{https://gist.github.com/josdirksen/5710f1e65605fdc85c52}
  *
  * @author Xander_C{AlessandroCevoli2@studio.unibo.it};
  * @groupname Arianna;
  *
  */
abstract class AbstractHttpServer(name: String) extends HttpServer {

     protected implicit val config = ConfigFactory.parseString(
          """
          http-akka-blocking-dispatcher {
            |type = Dispatcher
            |executor = "thread-pool-executor"
            |thread-pool-executor {
                |fixed-pool-size = 16
            }
            |throughput = 100
          }
        """.stripMargin).withFallback(ConfigFactory.load())

     implicit val akkaSubSystem = ActorSystem(name, config)

     protected implicit val materializer = ActorMaterializer()

     protected implicit val blockingDispatcher =
          akkaSubSystem.dispatchers.lookup("http-akka-blocking-dispatcher")

     // All the Futures are going to be handled by the Blocking Dispatcher
     val aSyncRequestHandler: HttpRequest => Future[HttpResponse] = null

     val streamRequestHandler: Route = null

     override def bindAsync(hostname: String, port: Int): Future[Http.ServerBinding] =
          Http().bindAndHandleAsync(aSyncRequestHandler, hostname, port)

     override def bindBlocking(hostname: String, port: Int): Future[Http.ServerBinding] =
          Http().bindAndHandle(streamRequestHandler, hostname, port)

     override def terminate(): Unit = akkaSubSystem.terminate()
}

object HttpServer {

     def apply(name: String): HttpServer =
          new AkkaHttpServer(name)
}

