import akka.actor.{ActorSystem, Props}
import akka.cluster.ClusterEvent.{CurrentClusterState, MemberRemoved, UnreachableMember}
import akka.cluster.{Cluster, Member, MemberStatus}
import akka.remote.UniqueAddress
import akka.testkit.{TestKit, TestProbe}
import com.scalefocus.cluster.SimpleClusterListener
import com.typesafe.config.ConfigFactory
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.{AnyWordSpec, AnyWordSpecLike}

class SimpleClusterListenerTest
  extends TestKit(ActorSystem("TestClusterSystem", ConfigFactory.parseString("akka.remote.artery.canonical.port=" + 2551)
    .withFallback(ConfigFactory.load("cluster-test-properties.conf"))))
  with AnyWordSpecLike with BeforeAndAfterAll {

  val system2 = ActorSystem("TestClusterSystem", ConfigFactory.parseString("akka.remote.artery.canonical.port=" + 2552)
    .withFallback(ConfigFactory.load("cluster-test-properties.conf")))
  val system3 = ActorSystem("TestClusterSystem", ConfigFactory.parseString("akka.remote.artery.canonical.port=" + 2553)
    .withFallback(ConfigFactory.load("cluster-test-properties.conf")))
//  val system4 = ActorSystem("TestClusterSystem", ConfigFactory.parseString("akka.remote.artery.canonical.port=" + 2554)
//    .withFallback(ConfigFactory.load("cluster-test-properties.conf")))


  override protected def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
    TestKit.shutdownActorSystem(system2)
    TestKit.shutdownActorSystem(system3)
//    TestKit.shutdownActorSystem(system4)
  }


  "A SimpleClusterListener" should {
    "respond correctly to MemberUp and UnreachableMember events" in {
      val listener1 = system.actorOf(Props[SimpleClusterListener])
      val listener2 = system2.actorOf(Props[SimpleClusterListener])
      val listener3 = system3.actorOf(Props[SimpleClusterListener])
//      val listener4 = system3.actorOf(Props[SimpleClusterListener])

      val cluster1: Cluster = Cluster(system)
      val cluster2: Cluster = Cluster(system2)
      val cluster3: Cluster = Cluster(system3)
//      val cluster4: Cluster = Cluster(system4)

      // Join nodes to the first node programmatically
      cluster2.join(cluster1.selfAddress) // Node 2 joins Node 1
      cluster3.join(cluster1.selfAddress) // Node 3 joins Node 1

      Thread.sleep(5000)

      assert(cluster1.state.members.size == 3)
      assert(cluster1.state.members.forall(_.status == MemberStatus.Up))

      TestKit.shutdownActorSystem(system3)

      val probe = TestProbe()(system)

      cluster1.subscribe(probe.ref, classOf[UnreachableMember], classOf[MemberRemoved])

      probe.expectMsgType[CurrentClusterState]

      Thread.sleep(15000)

      probe.expectMsgType[MemberRemoved]
//      probe.expectMsgType[UnreachableMember]

      // Confirm that the remaining cluster size is now 2
      assert(cluster1.state.members.size == 2)
      assert(cluster1.state.members.forall(_.status == MemberStatus.Up))
    }
  }
}
