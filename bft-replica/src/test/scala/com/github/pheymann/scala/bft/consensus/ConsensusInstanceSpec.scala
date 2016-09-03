package com.github.pheymann.scala.bft.consensus

import com.github.pheymann.scala.bft.consensus.CommitRound.Commit
import com.github.pheymann.scala.bft.consensus.ConsensusInstanceActor.FinishedConsensus
import com.github.pheymann.scala.bft.consensus.PrePrepareRound.PrePrepare
import com.github.pheymann.scala.bft.consensus.PrepareRound.Prepare
import com.github.pheymann.scala.bft.model.{ClientRequest, RequestDelivery}
import com.github.pheymann.scala.bft.replica.ReplicasMock.{CalledSendMessage, CalledSendRequest}
import com.github.pheymann.scala.bft.storage.LogStorageMock._
import com.github.pheymann.scala.bft.util.RequestDigitsGenerator
import com.github.pheymann.scala.bft.{BftReplicaConfig, BftReplicaSpec, WithActorSystem}
import org.specs2.concurrent.ExecutionEnv

import scala.concurrent.Future
import scala.concurrent.blocking

class ConsensusInstanceSpec(implicit ee: ExecutionEnv) extends BftReplicaSpec {

  sequential

  """The Consensus Instance and its two implementations for Leaders and Followers is the atomic unit
    |of the protocol handling the three consensus protocol and the internal state. It
  """.stripMargin should {
    "reach a consensus if all rounds have passed (leader)" in new WithActorSystem {
      val request     = new ClientRequest(0, 0, Array[Byte](0))
      val specContext = new ConsensusSpecContext(self, request, 1)

      import specContext.replicaContext

      val consensus = new LeaderConsensus()

      within(testDuration * 2) {
        val resultFut = Future(blocking(consensus ? request))

        sendMessages(specContext)

        expectMsg(CalledStart)
        expectMsg(CalledAddPrePrepare)
        expectMsg(CalledSendMessage)
        expectMsg(CalledSendRequest)
        expectMsg(CalledSendMessage)
        expectMsg(CalledAddPrepare)
        expectMsg(CalledSendMessage)
        expectMsg(CalledAddCommit)
        expectMsg(CalledFinish)
        expectMsg(FinishedConsensus)

        resultFut should beTrue.awaitFor(testDuration * 2)
      }
    }

    "reach a consensus if all rounds have passed (follower)" in new WithActorSystem {
      val request         = new ClientRequest(0, 0, Array[Byte](1))
      val message         = PrePrepare(0, 0, 0, RequestDigitsGenerator.generateDigits(request))
      val requestDelivery = RequestDelivery(0, 0, request)

      val specContext = new ConsensusSpecContext(self, request, 1)

      import specContext.replicaContext

      val consensus = new FollowerConsensus()

      within(testDuration * 2) {
        val resultFut = Future(blocking(consensus ? (message, requestDelivery)))

        sendMessages(specContext)

        expectMsg(CalledStart)
        expectMsg(CalledAddPrePrepare)
        expectMsg(CalledSendMessage)
        expectMsg(CalledAddPrepare)
        expectMsg(CalledSendMessage)
        expectMsg(CalledAddCommit)
        expectMsg(CalledFinish)
        expectMsg(FinishedConsensus)

        resultFut should beTrue.awaitFor(testDuration)
      }
    }

    "not reach a consensus when not all round conditions are fulfilled" in new WithActorSystem {
      val request     = new ClientRequest(0, 0, Array[Byte](2))
      val specContext = new ConsensusSpecContext(self, request, 1)

      import specContext.replicaContext
      import system.dispatcher

      val consensus = new LeaderConsensus()

      val resultFut = Future(blocking(consensus ? request))

      for (index <- 0 until (2 * BftReplicaConfig.expectedFaultyReplicas)) {
        specContext.replicaContext.messaging.messageBrokerRef ! Prepare(
          0L,
          specContext.sequenceNumber,
          specContext.view,
          specContext.requestDigits
        )
      }

      resultFut should beFalse.awaitFor(testDuration * 2)
    }
  }

  def sendMessages(specContext: ConsensusSpecContext) {
    for (index <- 0 until (2 * BftReplicaConfig.expectedFaultyReplicas)) {
      specContext.replicaContext.messaging.messageBrokerRef ! Prepare(
        index,
        specContext.sequenceNumber,
        specContext.view,
        specContext.requestDigits
      )
    }

    for (index <- 0 until (2 * BftReplicaConfig.expectedFaultyReplicas + 1)) {
      specContext.replicaContext.messaging.messageBrokerRef ! Commit(
        index,
        specContext.sequenceNumber,
        specContext.view,
        specContext.requestDigits
      )
    }
  }

}
