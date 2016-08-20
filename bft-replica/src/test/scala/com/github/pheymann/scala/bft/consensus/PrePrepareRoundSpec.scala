package com.github.pheymann.scala.bft.consensus

import akka.actor.Props
import com.github.pheymann.scala.bft.{BftReplicaSpec, WithActorSystem}
import com.github.pheymann.scala.bft.consensus.PrePrepareRound.{FinishedPrePrepare, JoinConsensus, StartConsensus}
import com.github.pheymann.scala.bft.util.CollectorStateObserver.CollectorsReady
import com.github.pheymann.scala.bft.util.{ClientRequest, RoundMessageExpectation, StorageMessageExpectation}

class PrePrepareRoundSpec extends BftReplicaSpec {
  
  sequential

  "The Pre-Prepare Round" should {
    "start a consensus as leader by sending the request and related message to all replicas" in new WithActorSystem {
      val request     = new ClientRequest(Array[Byte](0))
      val specContext = new ConsensusSpecContext(self, request, 3)

      import specContext.{consensusContext, replicaContext}

      specContext.collectors.initCollectors(
        RoundMessageExpectation(prePrepareNumber = 1, isRequestDelivery = true),
        StorageMessageExpectation(isStart = true, isPrePrepare = true)
      )

      val prePrepareRound = system.actorOf(Props(new PrePrepareRound()))

      within(testDuration) {
        prePrepareRound ! StartConsensus

        expectMsgAllOf(FinishedPrePrepare, CollectorsReady)
      }
    }

    "or join a already started consensus as follower" in new WithActorSystem {
      val request     = new ClientRequest(Array[Byte](1))
      val specContext = new ConsensusSpecContext(self, request, 3)

      import specContext.{consensusContext, replicaContext}

      specContext.collectors.initCollectors(
        RoundMessageExpectation(),
        StorageMessageExpectation(isStart = true, isPrePrepare = true)
      )

      val prePrepareRound = system.actorOf(Props(new PrePrepareRound()))

      within(testDuration) {
        specContext.collectors.setRoundCollectorReady()
        prePrepareRound ! JoinConsensus

        expectMsgAllOf(FinishedPrePrepare, CollectorsReady)
      }
    }

  }

}
