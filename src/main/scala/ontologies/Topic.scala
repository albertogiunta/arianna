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


//From Cell to Server
object ServerTopic extends Topic {
  override def topicName: String = "server"
}


