package com.github.pheymann.scala.bft.consensus

import com.github.pheymann.scala.bft.{BftReplicaSpec, WithActorSystem}
import com.github.pheymann.scala.bft.consensus.PrePrepareRound.PrePrepare
import com.github.pheymann.scala.bft.model.{ClientRequest, RequestDelivery}
import com.github.pheymann.scala.bft.util.RequestDigitsGenerator
import org.specs2.concurrent.ExecutionEnv

import scala.concurrent._

class FollowerConsensusSpec(implicit ee: ExecutionEnv) extends BftReplicaSpec {

  "The Follower Consensus" should {
    "only join a consensus if request and pre-prepare message are valid" in new WithActorSystem {
      val request         = new ClientRequest(0, 0, Array[Byte](0))
      val message         = PrePrepare(0L, 0, 0, RequestDigitsGenerator.generateDigits(request))

      val specContext = new ConsensusSpecContext(self, request, 2)

      import specContext.replicaContext

      val consensus = FollowerConsensus()

      Future(blocking(consensus ? (message, RequestDelivery(0, 0, request)))) should beTrue.awaitFor(testDuration)
      Future(blocking(consensus ? (message, RequestDelivery(1, 0, request)))) should beFalse.awaitFor(testDuration)
      Future(blocking(consensus ? (message, RequestDelivery(0, 1, request)))) should beFalse.awaitFor(testDuration)
      Future(blocking(consensus ? (message, RequestDelivery(0, 1, ClientRequest(0, 0, Array[Byte](1)))))) should beFalse.awaitFor(testDuration)
    }
  }

}
