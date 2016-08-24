package com.github.pheymann.scala.bft.consensus

import java.util.concurrent.TimeoutException

import com.github.pheymann.scala.bft.consensus.CommitRound.Commit
import com.github.pheymann.scala.bft.consensus.ConsensusInstance.FinishedConsensus
import com.github.pheymann.scala.bft.consensus.PrePrepareRound.{JoinConsensus, StartConsensus}
import com.github.pheymann.scala.bft.consensus.PrepareRound.Prepare
import com.github.pheymann.scala.bft.replica.ReplicasMock.{CalledSendMessage, CalledSendRequest}
import com.github.pheymann.scala.bft.storage.LogStorageMock._
import com.github.pheymann.scala.bft.{BftReplicaConfig, BftReplicaSpec, WithActorSystem}
import com.github.pheymann.scala.bft.util._

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

class ConsensusInstanceSpec extends BftReplicaSpec {

  sequential

  val expectMsgConsensus = Seq(CalledSendMessage, CalledSendRequest, CalledStart, CalledAddPrePrepare, CalledAddPrepare, CalledAddCommit, CalledFinish)

  """The Consensus Instance and its two implementations for Leaders and Followers is the atomic unit
    |of the protocol handling the three consensus protocol and the internal state. It
  """.stripMargin should {
    "reach a consensus if all rounds have passed for leader replica" in new WithActorSystem {
      val request     = new ClientRequest(Array[Byte](0))
      val specContext = new ConsensusSpecContext(self, request, 1)

      import specContext.replicaContext

      val consensus = new LeaderConsensus(request)

      within(testDuration * 2) {
        consensus.instanceRef ! StartConsensus

        sendMessages(consensus, specContext)

        expectMsgAllOf(expectMsgConsensus: _*)
        expectMsg(FinishedConsensus)
      }
    }

    "reach a consensus if all rounds have passed for follower replicas" in new WithActorSystem {
      val request     = new ClientRequest(Array[Byte](1))
      val specContext = new ConsensusSpecContext(self, request, 1)

      import specContext.replicaContext

      val consensus = new FollowerConsensus(request)

      within(10.seconds) {
        consensus.instanceRef ! JoinConsensus

        expectMsgAllOf(expectMsgConsensus.filter(_ == CalledSendRequest): _*)
        expectMsg(FinishedConsensus)
      }
    }

    "not reach a consensus when not all round conditions are fulfilled" in new WithActorSystem {
      val request     = new ClientRequest(Array[Byte](2))
      val specContext = new ConsensusSpecContext(self, request, 1)

      import specContext.replicaContext
      import system.dispatcher

      val consensus = new LeaderConsensus(request)

      val resultFut = Future {
        isConsensus(consensus)
      }

      for (index <- 0 until (2 * BftReplicaConfig.expectedFaultyReplicas))
        consensus.instanceRef ! Prepare(0L, specContext.sequenceNumber, specContext.view, specContext.requestDigits)

      Await.result(resultFut, 10.seconds) should beFalse
    }
  }

  def sendMessages(instance: ConsensusInstance, specContext: ConsensusSpecContext) {
    for (index <- 0 until (2 * BftReplicaConfig.expectedFaultyReplicas))
      instance.instanceRef ! Prepare(0L, specContext.sequenceNumber, specContext.view, specContext.requestDigits)

    for (index <- 0 until (2 * BftReplicaConfig.expectedFaultyReplicas + 1))
      instance.instanceRef ! Commit(0L, specContext.sequenceNumber, specContext.view, specContext.requestDigits)
  }

  def isConsensus(instance: ConsensusInstance): Boolean = {
    var isConsensus = false

    try {
      Await.result(instance.start(), testDuration)
      isConsensus = true
    }
    catch {
      case _: TimeoutException => instance.logAborted()
    }
    isConsensus
  }

}
