package akka.serialization.tests

import akka.serialization.{AriadneMessageSerializer, MessageSerializer}
import ontologies.messages.Location._
import ontologies.messages.MessageType.Handshake
import ontologies.messages._

/**
  * Created by Alessandro on 13/07/2017.
  */
object TestMessageSerializer extends App {
    
    var jsonStr: String = MessageType.Update.Subtype.Sensors
        .marshal(
            SensorList(
                InfoCell(0, "uri", "name",
                    Coordinates(Point(1, 1), Point(-1, -1), Point(-1, 1), Point(1, -1)),
                    Point(0, 0)
                ),
                List(Sensor(1, 2.0), Sensor(2, 1.55))
            )
        )
    
    var jsonStr2: String = MessageType.Handshake.Subtype.User2Cell.marshal(
        Empty()
    )
    
    println(jsonStr2)
    
    //    var toJsonObj: String => MessageContent = s => MessageType.Update.Subtype.Sensors.unmarshal(s)
    var toJsonObj: String => MessageContent = s => MessageType.Handshake.Subtype.User2Cell.unmarshal(s)
    
    val serializer = new AriadneMessageSerializer
    
    //    val message = AriadneMessage(
    //        Update,
    //        Update.Subtype.Sensors,
    //        Location.Cell >> Location.Server,
    //        toJsonObj(jsonStr)
    //    )
    
    val message = AriadneMessage(
        Handshake,
        Handshake.Subtype.User2Cell,
        Location.User >> Location.Cell,
        toJsonObj(jsonStr2)
    )
    
    println(message)
    println("Testing Serialization Utility...")
    val serial = MessageSerializer.serialize(message)
    val deserial = MessageSerializer.deserialize(serial)
    
    println(deserial == message)
    
    val serializedMessage = serializer.toBinary(message)
    val deserializedMessage = serializer.fromBinary(serializedMessage, serializer.manifest(message))
    println("Testing Binary Serializer...")
    println(serializedMessage.mkString("-"))
    println(deserializedMessage == message)
    println(deserializedMessage == deserial)
}
