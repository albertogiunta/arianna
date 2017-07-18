package cell.cluster

import akka.cluster.pubsub.DistributedPubSubMediator.Publish
import common.BasicPublisher
import ontologies._
import ontologies.messages.Location._
import ontologies.messages.MessageType._
import ontologies.messages._

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
    private val roomVertices: Coordinates = Coordinates(Point(1, 1),
        Point(-1, -1),
        Point(-1, 1),
        Point(1, -1))
    private val antennaPosition: Point = Point(0, 0)
    
    override protected def init(args: List[Any]) = {
        log.info("Hello there from {}!", name)
        mediator ! Publish(Topic.HandShakes,
            AriadneMessage(
                Handshake,
                Handshake.Subtype.Cell2Master,
                Location.Cell >> Location.Server,
                InfoCell(cellID, cellUri, cellName, roomVertices, antennaPosition)
            )
        )
    }

    override protected def receptive = {
        case msg@AriadneMessage(Handshake, Handshake.Subtype.Cell2Master, _, _) =>
            mediator ! Publish(Topic.HandShakes, msg)

        case msg@AriadneMessage(Update, Update.Subtype.Sensors, _, _) =>
            mediator ! Publish(Topic.Updates, msg)

        case msg@AriadneMessage(Update, Update.Subtype.Practicability, _, _) =>
            mediator ! Publish(Topic.Updates, msg)
            mediator ! Publish(Topic.Practicabilities, msg)

        case msg@AriadneMessage(Update, Update.Subtype.ActualLoad, _, cnt: ActualLoadUpdate) =>
            mediator ! Publish(Topic.Updates, msg)

        case _ => // Ignore
    }
}