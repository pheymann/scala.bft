package com.github.pheymann.scala.bft.consensus

import com.github.pheymann.scala.bft.{BftReplicaSpec, SpecContext, WithActorSystem}
import com.github.pheymann.scala.bft.consensus.PrePrepareRound.PrePrepare
import com.github.pheymann.scala.bft.model.{ClientRequest, RequestDelivery}
import org.specs2.concurrent.ExecutionEnv

import scala.concurrent._

class FollowerConsensusSpec(implicit ee: ExecutionEnv) extends BftReplicaSpec {

//  "The Follower Consensus" should {
//    "only join a consensus if request and pre-prepare message are valid" in new WithActorSystem {
//      val specContext = new SpecContext(self, 2)
//
//      val request = new ClientRequest(0, 0, Array[Byte](0))
//      val message = PrePrepare(0L, specContext.sequenceNumber, specContext.view)
//
//      import specContext.replicaContext
//
//      val consensus = FollowerConsensus()
//
//      Future(blocking(consensus ? (message, RequestDelivery(1, 0, request)))) should beFalse.awaitFor(testDuration)
//      Future(blocking(consensus ? (message, RequestDelivery(0, 1, request)))) should beFalse.awaitFor(testDuration)
//      Future(blocking(consensus ? (message, RequestDelivery(0, 1, ClientRequest(0, 0, Array[Byte](1)))))) should beFalse.awaitFor(testDuration)
//    }
//  }

}
