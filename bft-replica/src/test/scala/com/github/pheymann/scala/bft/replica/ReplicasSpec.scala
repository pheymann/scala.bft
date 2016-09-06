package com.github.pheymann.scala.bft.replica

import akka.actor.{ActorRef, ActorSystem, Props}
import com.github.pheymann.scala.bft.consensus.CommitRound.Commit
import com.github.pheymann.scala.bft.consensus.PrePrepareRound.PrePrepare
import com.github.pheymann.scala.bft.model.ClientRequest
import com.github.pheymann.scala.bft.replica.RemoteReplicaActorMock.{ReceivedConsensusMessage, ReceivedDataChunk, ReceivedStartStream}
import com.github.pheymann.scala.bft.util.AuthenticationDigitsGenerator
import com.github.pheymann.scala.bft.{BftReplicaSpec, WithActorSystem}

import scala.concurrent.duration._

class ReplicasSpec extends BftReplicaSpec {

  sequential

  class TestReplicas(val self: Replica, numOfRemoteReplicas: Int, specRef: ActorRef)
                    (implicit system: ActorSystem) extends Replicas {

    override private[replica] val remoteReplicaRefs: Seq[ActorRef] = {
      (0 until numOfRemoteReplicas).map { index =>
        system.actorOf(Props(new RemoteReplicaActorMock(specRef)), s"remote-actor-$index")
      }
    }

  }

  "A Replicas instance" should {
    "send consensus messages directly via akka to all remote replicas" in new WithActorSystem {
      val replica   = new Replica(0, 0, 0)
      val replicas  = new TestReplicas(replica, 3, self)

      within(testDuration) {
        replicas.sendMessage(Commit(replica.id, replica.sequenceNumber, replica.view, Array.empty))

        expectMsgAllOf(ReceivedConsensusMessage, ReceivedConsensusMessage, ReceivedConsensusMessage)
        expectNoMsg(noMessageDuration)
      }
    }

    "split-up a request into chunks and send these to all remote replicas" in new WithActorSystem {
      val replica   = new Replica(0, 0, 0)
      val replicas  = new TestReplicas(replica, 3, self)

      val request = ClientRequest(0, 0, Array[Byte](0, 1, 2, 3))
      val message = PrePrepare(replica.id, replica.sequenceNumber, replica.view, AuthenticationDigitsGenerator.generateDigits(request))

      within(testDuration) {
        replicas.sendRequest(request)

        // chunk size is 8 Bytes -> 5 chunks per remote replica
        expectMsgAllOf(
          ReceivedStartStream(5), ReceivedStartStream(5), ReceivedStartStream(5),
          ReceivedDataChunk, ReceivedDataChunk, ReceivedDataChunk, ReceivedDataChunk, ReceivedDataChunk,
          ReceivedDataChunk, ReceivedDataChunk, ReceivedDataChunk, ReceivedDataChunk, ReceivedDataChunk,
          ReceivedDataChunk, ReceivedDataChunk, ReceivedDataChunk, ReceivedDataChunk, ReceivedDataChunk
        )

        expectNoMsg(noMessageDuration)
      }
    }
  }

}
