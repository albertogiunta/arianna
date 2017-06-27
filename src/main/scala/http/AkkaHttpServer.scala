package http

/**
  * Created by Alessandro on 27/06/2017.
  */


import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory

import scala.io.StdIn

class AkkaHttpServer {
    
}

object AkkaHttpServer {
    
    def main(args: Array[String]) {
        implicit val customConfig = ConfigFactory.parseString(
            """
              http-akka-blocking-dispatcher {
                |type = Dispatcher
                |executor = "thread-pool-executor"
                |thread-pool-executor {
                    |fixed-pool-size = 16
                }
                |throughput = 100
              }
            """.stripMargin)
        implicit val defaultConfig = ConfigFactory.load()
        
        implicit val config = customConfig.withFallback(defaultConfig)
        
        implicit val system = ActorSystem("Arianna-Http-Master", config)
        
        implicit val materializer = ActorMaterializer()
        // needed for the future map/flatmap in the end
        //implicit val executionContext = system.dispatcher
        implicit val blockingDispatcher = system.dispatchers.lookup("http-akka-blocking-dispatcher")
        
        val requestHandler: HttpRequest => HttpResponse = {
            case HttpRequest(GET, Uri.Path("/"), _, _, _) =>
                HttpResponse(entity = HttpEntity(
                    ContentTypes.`text/html(UTF-8)`,
                    "<html><body>Hello world!</body></html>"))
            
            case HttpRequest(GET, Uri.Path("/nodes"), _, _, _) =>
                HttpResponse(entity = HttpEntity(
                    ContentTypes.`text/html(UTF-8)`,
                    "<html><body>Here are the active nodes</body></html>"))
            
            case HttpRequest(GET, Uri.Path("/logs"), _, _, _) =>
                HttpResponse(entity = HttpEntity(
                    ContentTypes.`text/html(UTF-8)`,
                    "<html><body>Here are the system logs!</body></html>"))
            
            case r: HttpRequest =>
                r.discardEntityBytes() // important to drain incoming HTTP Entity stream
                HttpResponse(404, entity = "Unknown resource!")
        }
        
        val bindingFuture = Http().bindAndHandleSync(requestHandler, "localhost", 8080)
        
        println(s"Server online at http://localhost:8080/\n")
        
        while(StdIn.readLine().toLowerCase != "exit"){
            //Do Nothing, just hang in there
        }
        
        bindingFuture
            .flatMap(_.unbind()) // trigger unbinding from the port
            .onComplete(_ => system.terminate()) // and shutdown when done
        
    }
}
