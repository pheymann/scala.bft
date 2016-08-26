package com.github.pheymann.scala.bft.consensus

import com.github.pheymann.scala.bft.{BftReplicaSpec, WithActorSystem}
import com.github.pheymann.scala.bft.consensus.PrePrepareRound.PrePrepare
import com.github.pheymann.scala.bft.util.{ClientRequest, RequestDelivery}

class FollowerConsensusSpec extends BftReplicaSpec {

  "The Follower Consensus" should {
    "only create an instance of its type if the given request and pre-prepare message are valid" in new WithActorSystem {
      val request     = new ClientRequest(0, 0, Array[Byte](0))
      val specContext = new ConsensusSpecContext(self, request, 2)
      val message     = PrePrepare(0L, specContext.sequenceNumber, specContext.view, specContext.requestDigits)

      import specContext.replicaContext

      FollowerConsensus.createIfValid(
        message,
        RequestDelivery(specContext.sequenceNumber, specContext.view, request)
      ).isDefined should beTrue

      FollowerConsensus.createIfValid(
        message,
        RequestDelivery(specContext.sequenceNumber + 1, specContext.view, request)
      ) === None

      FollowerConsensus.createIfValid(
        message,
        RequestDelivery(specContext.sequenceNumber, specContext.view + 1, request)
      ) === None

      val specContextInvalid = new ConsensusSpecContext(self, request, logHasAcceptedOrUnknown = false)

      FollowerConsensus.createIfValid(
        message,
        RequestDelivery(specContextInvalid.sequenceNumber, specContextInvalid.view, ClientRequest(0, 0, Array[Byte](1)))
      )(system, specContextInvalid.replicaContext) === None
    }
  }

}
