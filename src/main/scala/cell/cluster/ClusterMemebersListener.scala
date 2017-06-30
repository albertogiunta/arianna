package cell.cluster

import java.io.File
import java.nio.file.Paths

import akka.actor.{Actor, ActorLogging, ActorSystem, Address}
import akka.cluster.ClusterEvent.{CurrentClusterState, MemberEvent, MemberRemoved, MemberUp}
import akka.cluster.{Cluster, MemberStatus}
import com.typesafe.config.ConfigFactory

/**
  * This actor implements the seeds node behaviour to automatic manage
  * actors joining in the cluster
  * Created by Matteo Gabellini on 29/06/2017.
  * Code based on akka code from official documentation
  */
class ClusterMemebersListener extends Actor with ActorLogging {

  val path2Project = Paths.get("").toFile.getAbsolutePath
  val path2Config = path2Project + "/src/main/scala/testCell.conf"

  implicit val config = ConfigFactory.parseFile(new File(path2Config))
    .withFallback(ConfigFactory.load()).resolve()

  implicit val system = ActorSystem("Arianna-Cluster-Master", config)
  val cluster = Cluster(system)

  override def preStart(): Unit =
    cluster.subscribe(self, classOf[MemberEvent])

  override def postStop(): Unit =
    cluster unsubscribe self

  var nodes = Set.empty[Address]

  def receive = {
    case state: CurrentClusterState =>
      nodes = state.members.collect {
        case m if m.status == MemberStatus.Up => m.address
      }
    case MemberUp(member) =>
      nodes += member.address
      log.info("Member is Up: {}. {} nodes in cluster",
        member.address, nodes.size)
    case MemberRemoved(member, _) =>
      nodes -= member.address
      log.info("Member is Removed: {}. {} nodes cluster",
        member.address, nodes.size)
    case _: MemberEvent => // ignore
  }
}
