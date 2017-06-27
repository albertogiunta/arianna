package http

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory

import scala.concurrent.Future
import scala.io.StdIn

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
class AkkaHttpServer(name : String) {
    
   private implicit val config = ConfigFactory.parseString(
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
    
    private implicit val materializer = ActorMaterializer()
    
    private implicit val blockingDispatcher = akkaSubSystem.dispatchers.lookup("http-akka-blocking-dispatcher")
    
    def bind(hostname : String, port : Int) : Future[Http.ServerBinding] =
        Http().bindAndHandleAsync(aSyncRequestHandler, hostname, port)
    
    // All the Futures are going to be handled by the Blocking Dispatcher
    def aSyncRequestHandler(request: HttpRequest): Future[HttpResponse] = request match {
        case HttpRequest(GET, Uri.Path("/"), _, _, _) =>
            
            Future {
                HttpResponse(entity = HttpEntity(
                    ContentTypes.`text/html(UTF-8)`,
                    "<html><body>Hello world!</body></html>"))
            }
        
        case HttpRequest(GET, Uri.Path("/nodes"), _, _, _) =>
            Future {
                HttpResponse(entity = HttpEntity(
                    ContentTypes.`text/html(UTF-8)`,
                    "<html><body>Here are the active nodes</body></html>"))
            }
        
        case HttpRequest(GET, Uri.Path("/logs"), _, _, _) =>
            
            Future {
                HttpResponse(entity = HttpEntity(
                    ContentTypes.`text/html(UTF-8)`,
                    "<html><body>Here are the system logs!</body></html>"))
            }
        // To be modified with PUT or POST
        case HttpRequest(GET, Uri.Path("/subscribe"), _, _, _) =>
            
            Future {
                request.uri.query().foreach(p => println(p._1 + "=>" + p._2))
                
                HttpResponse(entity = HttpEntity(
                    ContentTypes.`text/html(UTF-8)`,
                    "<html><body>Here is the Map</body></html>"))
            }
        
        case r: HttpRequest =>
            r.discardEntityBytes() // important to drain incoming HTTP Entity stream
            Future {
                HttpResponse(404, entity = "Unknown resource!")
            }
    }
    
    def terminate() : Unit = akkaSubSystem.terminate()
}


object AkkaHttpServer {
    
    def apply(name: String) : AkkaHttpServer =
        new AkkaHttpServer(name)
}

object AkkaServerBootstrap extends App{
    
    override def main(args: Array[String]) {
        
        val httpServer = AkkaHttpServer("Arianna.Master.http")
        val bindingFuture = httpServer.bind("localhost", 8080)
        
        println(s"Server online at http://localhost:8080/\n")
        
        while(StdIn.readLine().toLowerCase != "exit"){
            //Do Nothing, just hang in there
        }
        
        bindingFuture
            .flatMap(_.unbind()) // trigger unbinding from the port
            .onComplete(_ => httpServer.terminate()) // and shutdown when done
        
    }
}
