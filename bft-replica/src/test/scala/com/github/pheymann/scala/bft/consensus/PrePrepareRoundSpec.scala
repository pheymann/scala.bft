package com.github.pheymann.scala.bft.consensus

import akka.actor.Props
import akka.pattern.ask
import com.github.pheymann.scala.bft.BftReplicaSpec
import com.github.pheymann.scala.bft.consensus.PrePrepareRound.{FinishedPrePrepare, JoinConsensus, StartConsensus}
import com.github.pheymann.scala.bft.util.{RoundMessageExpectation, StorageMessageExpectation}

import scala.concurrent.Await

class PrePrepareRoundSpec extends BftReplicaSpec {

  "The Pre-Prepare Round" should {
    "start a consensus as leader by sending the request and related message to all replicas" in new SpecContext {
      initCollectors(
        RoundMessageExpectation(prePrepareNumber = 1, isRequestDelivery = true),
        StorageMessageExpectation(isStart = true, isPrePrepare = true)
      )

      val prePrepareRound = system.actorOf(Props(new PrePrepareRound()))

      Await.result(prePrepareRound ? StartConsensus, timeoutDuration) === FinishedPrePrepare
      observedResult should beTrue
    }

    "or join a already started consensus as follower" in new SpecContext {
      initCollectors(
        RoundMessageExpectation(),
        StorageMessageExpectation(isStart = true, isPrePrepare = true)
      )

      val prePrepareRound = system.actorOf(Props(new PrePrepareRound()))

      Await.result(prePrepareRound ? JoinConsensus, timeoutDuration) === FinishedPrePrepare
      observedResult should beTrue
    }

  }

}
