package com.github.pheymann.scala.bft.consensus

import com.github.pheymann.scala.bft.{BftReplicaSpec, WithActorSystem}
import com.github.pheymann.scala.bft.consensus.PrePrepareRound.{PrePrepare, RequestDelivery}
import com.github.pheymann.scala.bft.util.{ClientRequest, RoundMessageExpectation, StorageMessageExpectation}

class FollowerConsensusSpec extends BftReplicaSpec {

  "The Follower Consensus" should {
    "only create an instance of its type if the given request and pre-prepare message are valid" in new WithActorSystem {
      val request     = new ClientRequest(Array[Byte](0))
      val specContext = new ConsensusSpecContext(self, request, 2)
      val message     = PrePrepare(0L, specContext.sequenceNumber, specContext.view, specContext.requestDigits)

      import specContext.replicaContext

      specContext.collectors.initCollectors(RoundMessageExpectation.forValidConsensus, StorageMessageExpectation.forValidConsensus)

      FollowerConsensus.createIfValid(
        message,
        RequestDelivery(request, specContext.sequenceNumber, specContext.view)
      ).isDefined should beTrue

      FollowerConsensus.createIfValid(
        message,
        RequestDelivery(request, specContext.sequenceNumber + 1, specContext.view)
      ) === None

      FollowerConsensus.createIfValid(
        message,
        RequestDelivery(request, specContext.sequenceNumber, specContext.view + 1)
      ) === None

      val specContextInvalid = new ConsensusSpecContext(self, request, logHasAcceptedOrUnknown = false)

      FollowerConsensus.createIfValid(
        message,
        RequestDelivery(ClientRequest(Array[Byte](1)), specContextInvalid.sequenceNumber, specContextInvalid.view)
      )(system, specContextInvalid.replicaContext) === None
    }
  }

}