package com.github.pheymann.scala.bft.consensus

import akka.actor.Props
import akka.pattern.ask
import com.github.pheymann.scala.bft.{BftReplicaConfig, BftReplicaSpec, WithActorSystem}
import com.github.pheymann.scala.bft.consensus.PrePrepareRound.{FinishedPrePrepare, JoinConsensus, StartConsensus}
import com.github.pheymann.scala.bft.util.{ClientRequest, RoundMessageExpectation, StorageMessageExpectation}
import org.specs2.concurrent.ExecutionEnv

import scala.concurrent.Await

class PrePrepareRoundSpec extends BftReplicaSpec {
  
  sequential

  "The Pre-Prepare Round" should {
    "start a consensus as leader by sending the request and related message to all replicas" in new WithActorSystem {
      val request     = new ClientRequest(Array[Byte](0))
      val specContext = new ConsensusSpecContext(request)

      import specContext.{consensusContext, replicaContext}

      implicit val _testTimeout = testTimeout

      specContext.collectors.initCollectors(
        RoundMessageExpectation(prePrepareNumber = 1, isRequestDelivery = true),
        StorageMessageExpectation(isStart = true, isPrePrepare = true)
      )

      val prePrepareRound = system.actorOf(Props(new PrePrepareRound()))

      Await.result(prePrepareRound ? StartConsensus, testDuration) === FinishedPrePrepare
      Await.result(specContext.collectors.observedResult, testDuration) should beTrue
    }

    "or join a already started consensus as follower" in new WithActorSystem {
      val request     = new ClientRequest(Array[Byte](1))
      val specContext = new ConsensusSpecContext(request)

      import specContext.{consensusContext, replicaContext}

      implicit val _testTimeout = testTimeout

      specContext.collectors.initCollectors(
        RoundMessageExpectation(),
        StorageMessageExpectation(isStart = true, isPrePrepare = true)
      )

      val prePrepareRound = system.actorOf(Props(new PrePrepareRound()))

      Await.result(prePrepareRound ? JoinConsensus, testDuration) === FinishedPrePrepare
      Await.result(specContext.collectors.observedResult, testDuration) should beTrue
    }

  }

}
