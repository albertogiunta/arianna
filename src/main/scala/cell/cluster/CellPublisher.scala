package cell.cluster

import akka.cluster.pubsub.DistributedPubSubMediator.Publish
import common.BasicPublisher
import ontologies._
import ontologies.messages.Location._
import ontologies.messages.MessageType._
import ontologies.messages._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Actor that manages the sending of messages to the main server
  * Created by Matteo Gabellini on 29/06/2017.
  */
class CellPublisher extends BasicPublisher {
    
    /*
    * Provisional constants that represents the cell's info.
    * In the future this info will be modelled in a different way
    * */
    private val cellID: Int = 12345
    private val cellUri: String = "uri1"
    private val cellName: String = "cell1"
    private val roomVertices: Coordinates = Coordinates(
        Point(0, 0),
        Point(2, 2),
        Point(0, 2),
        Point(2, 0)
    )
    
    private val antennaPosition: Point = Point(0, 0)
    
    override protected def init(args: List[Any]) = {
        log.info("Hello there from {}!", name)
    
        log.info("Sending Handshake to Master...")
    
        Future {
            Thread.sleep(1000)
        }.onComplete(_ =>
            mediator ! Publish(Topic.HandShakes,
                AriadneMessage(
                    Handshake,
                    Handshake.Subtype.CellToMaster,
                    Location.Cell >> Location.Server,
                    SensorsUpdate(
                        InfoCell(cellID, cellUri, cellName, roomVertices, antennaPosition),
                        List()
                    )
                )
            )
        )
    }
    
    override protected def receptive = {
        case msg@AriadneMessage(Handshake, Handshake.Subtype.CellToMaster, _, _) =>
            mediator ! Publish(Topic.HandShakes, msg)

        case msg@AriadneMessage(Update, Update.Subtype.Sensors, _, _) =>
            mediator ! Publish(Topic.Updates, msg)

        case msg@AriadneMessage(Update, Update.Subtype.Practicability, _, _) =>
    
            mediator ! Publish(Topic.Practicabilities, msg)

        case msg@AriadneMessage(Update, Update.Subtype.CurrentPeople, _, _) =>
            mediator ! Publish(Topic.Updates, msg)

        case _ => // Ignore
    }
}