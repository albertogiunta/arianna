package ontologies

import ontologies.MessageType._

/**
  * Created by Xander_C on 03/07/2017.
  */
trait MessageType {
    
    val typeName: String
    
    override def toString: String = typeName
    
    override def equals(obj: scala.Any) = obj match {
        case o: MessageType => o.toString == this.toString
        case o: String => o == this.toString
    }
}

//final case class AriadneMessageType(override val typeName : String) extends MessageType

object MessageType {
    
    object Init extends MessageType {
        
        override val typeName = "Init"
        
        object Subtype {
            
            object Basic extends MessageSubtype {
                
                override val subtypeName = "Basic"
                
                def marshal(json: String): Any = null
                
                def unmarshal(obj: Any): String = null
            }
            
        }
        
    }
    
    object Alarm extends MessageType {
        
        override val typeName = "Alarm"
        
        object Subtype {
            
            object Basic extends MessageSubtype {
                
                override val subtypeName = "Basic"
                
                def marshal(json: String): Any = null
                
                def unmarshal(obj: Any): String = null
            }
            
        }
        
    }
    
    object Handshake extends MessageType {
        
        override val typeName = "Handshake"
        
        object Subtype {
            
            object Basic extends MessageSubtype {
                
                override val subtypeName = "Basic"
                
                def marshal(json: String): Any = null
                
                def unmarshal(obj: Any): String = null
            }
            
        }
        
    }
    
    object Route extends MessageType {
        
        override val typeName = "Route"
        
        object Subtype {
            
            object Basic extends MessageSubtype {
                
                override val subtypeName = "Basic"
                
                def marshal(json: String): Any = null
                
                def unmarshal(obj: Any): String = null
            }
            
            object Escape extends MessageSubtype {
                
                override val subtypeName = "Escape"
                
                def marshal(json: String): Any = null
                
                def unmarshal(obj: Any): String = null
            }
            
        }
        
    }
    
    object Topology extends MessageType {
        
        override val typeName = "Topology"
        
        object Subtype {
            
            object Planimetrics extends MessageSubtype {
                
                override val subtypeName = "Planimetrics"
                
                def marshal(json: String): Any = null
                
                def unmarshal(obj: Any): String = null
            }
            
            object RealTopology extends MessageSubtype {
                
                override val subtypeName = "RealTopology"
                
                def marshal(json: String): Any = null
                
                def unmarshal(obj: Any): String = null
            }
            
            object Topology4Cell extends MessageSubtype {
                
                override val subtypeName = "Topology4Cell"
                
                def marshal(json: String): Any = null
                
                def unmarshal(obj: Any): String = null
            }
            
            object Topology4User extends MessageSubtype {
                
                override val subtypeName = "Topology4User"
                
                def marshal(json: String): Any = null
                
                def unmarshal(obj: Any): String = null
            }
            
        }
        
    }
    
    object Update extends MessageType {
        
        override val typeName = "Update"
        
        object Subtype {
            
            object Sensors extends MessageSubtype {
                
                override val subtypeName = "Sensors"
                
                def marshal(json: String): Any = null
                
                def unmarshal(obj: Any): String = null
            }
            
            object Practicability extends MessageSubtype {
                
                override val subtypeName = "Practicability"
                
                def marshal(json: String): Any = null
                
                def unmarshal(obj: Any): String = null
            }
            
            object Position extends MessageSubtype {
                
                override val subtypeName = "Position"
                
                def marshal(json: String): Any = null
                
                def unmarshal(obj: Any): String = null
            }
            
            object CellOccupation extends MessageSubtype {
                
                override val subtypeName = "CellOccupation"
                
                def marshal(json: String): Any = null
                
                def unmarshal(obj: Any): String = null
            }
            
        }
        
    }
    
    implicit def MessageType2String(msg: MessageType): String = msg.toString
    
    implicit def String2MessageType(str: String): MessageType = MessageTypeFactory(str)
    
}

object MessageTypeFactory {
    
    def apply(typeName: String): MessageType = typeName.toLowerCase match {
        case t if t == Init.toString.toLowerCase => Init
        case t if t == Route.toString.toLowerCase => Route
        case t if t == Alarm.toString.toLowerCase => Alarm
        case t if t == Topology.toString.toLowerCase => Topology
        case t if t == Handshake.toString.toLowerCase => Handshake
        case t if t == Update.toString.toLowerCase => Update
        
        case _ => null
    }
}

object TestMessageType extends App {
    
    //MessageType.Alarm.typeName
    
    //MessageType.Update.Subtype.Sensors
}