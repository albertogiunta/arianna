package ontologies

/**
  * Created by Matteo Gabellini on 28/06/2017.
  */
trait Topic {
  def topicName: String
}

object AlarmTopic extends Topic {
  override def topicName: String = "alarm"
}

object TopologyTopic extends Topic {
  override def topicName: String = "topology"
}


