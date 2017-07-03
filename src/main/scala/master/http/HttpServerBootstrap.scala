package master.http

import scala.concurrent.ExecutionContext.Implicits.global
import scala.io.StdIn

/**
  * Created by Alessandro on 28/06/2017.
  */
object HttpServerBootstrap extends App {
    
    override def main(args: Array[String]) {
        val hostname = "localhost"
        val port = 8080
        val name = "Arianna-Master-master"
        val httpServer = HttpServer(name)
        //val bindingFuture = httpServer.bindWithFuture(hostname, port)
        
        val bindingFuture = httpServer.bindBlocking(hostname, port)

        println(s"$name server is online at master.http://$hostname:$port\n")
        
        while (StdIn.readLine().toLowerCase != "exit") {
            //Do Nothing, just hang in there
        }
        
        bindingFuture
            .flatMap(_.unbind()) // trigger unbinding from the port
            .onComplete(_ => httpServer.terminate()) // and shutdown when done
        
    }
}
