package master.http

import akka.http.scaladsl.model.HttpMethods.GET
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

import scala.concurrent.Future

/**
  * An Akka Http Server, implementing both Future-based and DSL Route-based Request Handler
  */
class AkkaHttpServer(name: String) extends AbstractHttpServer(name) {
    
    override val aSyncRequestHandler: HttpRequest => Future[HttpResponse] = {
        case HttpRequest(GET, Uri.Path("/"), _, _, _) =>
    
            Future {
                HttpResponse(entity = HttpEntity(
                    ContentTypes.`text/html(UTF-8)`,
                    "<html><body>Hello Theseus!</body></html>"))
            }

        case HttpRequest(GET, Uri.Path("/logs"), _, _, _) =>
    
            Future {
                HttpResponse(entity = HttpEntity(
                    ContentTypes.`text/html(UTF-8)`,
                    "<html><body>Here are the system logs!</body></html>"))
            }

        // To be modified with PUT or POST
        case request@HttpRequest(_, Uri.Path("/REST/subscribe"), _, _, _) =>
            
            Future {
                request.uri.query().foreach(p => println(p._1 + "=>" + p._2))
    
                HttpResponse(entity = HttpEntity(
                    ContentTypes.`text/html(UTF-8)`,
                    "<html><body>Here is the Map</body></html>"))
            }

        case HttpRequest(GET, Uri.Path("/REST/cells"), _, _, _) =>
    
            Future {
                HttpResponse(entity = HttpEntity(
                    ContentTypes.`text/html(UTF-8)`,
                    "<html><body>Here are the active nodes</body></html>"))
            }

        case r: HttpRequest =>
            r.discardEntityBytes() // important to drain incoming HTTP Entity stream
            Future {
                HttpResponse(404, entity = "Unknown resource!")
            }
    }
    
    override val streamRequestHandler: Route =
        get {
            pathSingleSlash {
                complete {
                    Future {
                        HttpEntity(ContentTypes.`text/html(UTF-8)`,
                            "<html><body>Hello Theseus!</body></html>")
                    }
                }
            } ~ path("logs") {
                complete {
                    Future {
                        HttpEntity(
                            ContentTypes.`text/html(UTF-8)`,
                            "<html><body>Here are the system logs!</body></html>"
                        )
                    }
                }
            } ~ path("cells") {
                complete {
                    Future {
                        HttpEntity(
                            ContentTypes.`text/html(UTF-8)`,
                            "<html><body>Here are all the data of the cells!</body></html>"
                        )
                    }
                }
            } ~ path("cells" / IntNumber) { cellID => {
                complete {
                    Future {
                        HttpEntity(
                            ContentTypes.`text/html(UTF-8)`,
                            s"<html><body>Here are all the data from cell with ID:$cellID!</body></html>"
                        )
                    }
                }
            }
            } ~ path("cells" / IntNumber / "sensors") { cellID => {
                complete {
                    Future {
                        HttpEntity(
                            ContentTypes.`text/html(UTF-8)`,
                            s"<html><body>Here are all the data from the sensors of the cell with ID:$cellID!</body></html>"
                        )
                    }
                }
            }
            } ~ path("cells" / IntNumber / "sensors" / IntNumber) { (cellID, sensorID) => {
                complete {
                    Future {
                        HttpEntity(
                            ContentTypes.`text/html(UTF-8)`,
                            s"<html><body>Here are the data for the Sensor of ID:$sensorID</body></html>"
                        )
                    }
                }
            }
            }
        } ~ put {
            path("subscribe") {
                parameters('name, 'something.?) { (name, something) =>
                    complete {
                        Future {
                            HttpEntity(ContentTypes.`text/html(UTF-8)`,
                                s"<html><body>Subscribed as $name and $something!</body></html>")
                        }
                    }
                }
            }
        } ~ complete {
            Future {
                //discardEntityBytes() // important to drain incoming HTTP Entity stream
                HttpResponse(404, entity = "Unknown resource!")
            }
        }
}