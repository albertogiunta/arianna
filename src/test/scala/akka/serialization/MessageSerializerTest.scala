package akka.serialization

import org.scalatest.{BeforeAndAfter, FunSuite}
import system.ontologies.messages.Location._
import system.ontologies.messages.MessageType.Update
import system.ontologies.messages._

/**
  * Created by Alessandro on 13/07/2017.
  */
class MessageSerializerTest extends FunSuite with BeforeAndAfter {
    
    var jsonStr: String = MessageType.Update.Subtype.Sensors
        .marshal(
            SensorsInfoUpdate(
                CellInfo("uri", 0),
                List(SensorInfo(1, 2.0), SensorInfo(2, 1.55))
            )
        )
    
    val toJsonObj: (MessageSubtype[MessageContent], String) => MessageContent = (sub, cnt) => sub.unmarshal(cnt)
    
    implicit val serializer: AriadneMessageSerializer = new AriadneMessageSerializer
    
    val messageForTest = AriadneMessage(
        Update,
        Update.Subtype.Sensors,
        Location.Cell >> Location.Master,
        toJsonObj(Update.Subtype.Sensors, jsonStr)
    )
    
    //    val messageForTest: Message[MessageContent] = AriadneMessage(
    //        Handshake,
    //        Acknowledgement,
    //        Location.Master >> Location.Cell,
    //        Empty()
    //    )
    
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
