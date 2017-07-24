package akka.serialization.tests

import akka.serialization.{AriadneMessageSerializer, MessageSerializer}
import ontologies.messages.Location._
import ontologies.messages.MessageType.Update
import ontologies.messages._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}

/**
  * Created by Alessandro on 13/07/2017.
  */
@RunWith(classOf[JUnitRunner])
class MessageSerializerTest extends FunSuite with BeforeAndAfter {
    
    var jsonStr: String = MessageType.Update.Subtype.Sensors
        .marshal(
            SensorsUpdate(
                InfoCell(0, "uri", "name",
                    Coordinates(Point(1, 1), Point(-1, -1), Point(-1, 1), Point(1, -1)),
                    Point(0, 0)
                ),
                List(Sensor(1, 2.0, 0.0, 0.0), Sensor(2, 1.55, 0.0, 0.0))
            )
        )
    
    val toJsonObj: (MessageSubtype, String) => MessageContent = (sub, cnt) => sub.unmarshal(cnt)
    
    implicit val serializer = new AriadneMessageSerializer
    
    val messageForTest = AriadneMessage(
        Update,
        Update.Subtype.Sensors,
        Location.Cell >> Location.Server,
        toJsonObj(Update.Subtype.Sensors, jsonStr)
    )
    
    test("Testing Serialization Utility...") {
        
        val serial: Array[Byte] = MessageSerializer.serialize(messageForTest)
        val deserial: Message[MessageContent] = MessageSerializer.deserialize(serial)
        
        assert(deserial == messageForTest)
    }
    
    test("Testing Binary Serializer...") {
        
        val serializedMessage: Array[Byte] = serializer.toBinary(messageForTest)
        val deserializedMessage: AnyRef = serializer.fromBinary(serializedMessage, serializer.manifest(messageForTest))
        
        assert(deserializedMessage == messageForTest)
    }
    
    
    test("Testing cross-binaries Serialization...") {
        
        val serial: Array[Byte] = MessageSerializer.serialize(messageForTest)
        val deserial: Message[MessageContent] = MessageSerializer.deserialize(serial)
        
        val serializedMessage: Array[Byte] = serializer.toBinary(messageForTest)
        val deserializedMessage: AnyRef = serializer.fromBinary(serializedMessage, serializer.manifest(messageForTest))
        
        assert(deserializedMessage == deserial)
    }
}
