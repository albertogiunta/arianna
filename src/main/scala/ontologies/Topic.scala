package ontologies

/**
  * Created by Matteo Gabellini on 28/06/2017.
  */
trait Topic {
    def topicName: String
}

//From Server to Cells
object AlarmTopic extends Topic {
    override def topicName: String = "alarm"
}

object TopologyTopic extends Topic {
    override def topicName: String = "topology"
}


//From Cells to Server
object HandShakeTopic extends Topic {
    override def topicName: String = "handshake"
}

object SensorUpdateTopic extends Topic {
    override def topicName: String = "sensor"
}

object PracticabilityTopic extends Topic {
    override def topicName: String = "practicability"
}

object CellDataTopic extends Topic {
    override def topicName: String = "weight"
}
