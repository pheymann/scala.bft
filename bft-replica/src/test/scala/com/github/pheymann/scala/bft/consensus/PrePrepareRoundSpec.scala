package com.github.pheymann.scala.bft.consensus

import akka.actor.Props
import com.github.pheymann.scala.bft.{BftReplicaSpec, WithActorSystem}
import com.github.pheymann.scala.bft.consensus.PrePrepareRound.{FinishedPrePrepare, JoinConsensus, StartConsensus}
import com.github.pheymann.scala.bft.replica.ReplicasMock.{CalledSendMessage, CalledSendRequest}
import com.github.pheymann.scala.bft.storage.LogStorageMock.{CalledAddPrePrepare, CalledStart}
import com.github.pheymann.scala.bft.util.ClientRequest

class PrePrepareRoundSpec extends BftReplicaSpec {
  
  sequential

  "The Pre-Prepare Round" should {
    "start a consensus as leader by sending the request and related message to all replicas" in new WithActorSystem {
      val request     = new ClientRequest(0, 0, Array[Byte](0))
      val specContext = new ConsensusSpecContext(self, request, 3)

      import specContext.{consensusContext, replicaContext}

      val prePrepareRound = system.actorOf(Props(new PrePrepareRound()))

      within(testDuration) {
        prePrepareRound ! StartConsensus

        expectMsg(CalledStart)
        expectMsg(CalledAddPrePrepare)
        expectMsg(CalledSendMessage)
        expectMsg(CalledSendRequest)
        expectMsg(FinishedPrePrepare)
      }
    }

    "or join a already started consensus as follower" in new WithActorSystem {
      val request     = new ClientRequest(0, 0, Array[Byte](1))
      val specContext = new ConsensusSpecContext(self, request, 3)

      import specContext.{consensusContext, replicaContext}

      val prePrepareRound = system.actorOf(Props(new PrePrepareRound()))

      within(testDuration) {
        prePrepareRound ! JoinConsensus

        expectMsg(CalledStart)
        expectMsg(CalledAddPrePrepare)
        expectMsg(FinishedPrePrepare)
      }
    }

  }

}
