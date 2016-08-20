package com.github.pheymann.scala.bft.consensus

import akka.actor.Props
import com.github.pheymann.scala.bft.{BftReplicaConfig, BftReplicaSpec, WithActorSystem}
import com.github.pheymann.scala.bft.consensus.CommitRound.{Commit, FinishedCommit, StartCommit}
import com.github.pheymann.scala.bft.consensus.PrepareRound.{FinishedPrepare, Prepare, StartPrepare}
import com.github.pheymann.scala.bft.util.CollectorStateObserver.CollectorsReady
import com.github.pheymann.scala.bft.util.{ClientRequest, RoundMessageExpectation, StorageMessageExpectation}

class ConsensusRoundSpec extends BftReplicaSpec {

  sequential

  """The two different implementations of ConsensusRound are tested in this Spec.
    |
    |A consensus round""".stripMargin should {
    "(Prepare|Commit Round) distribute the current round message to all replicas on start" in new WithActorSystem {
      val request     = new ClientRequest(Array[Byte](0))
      val specContext = new ConsensusSpecContext(self, request)

      import specContext.{consensusContext, replicaContext}

      specContext.collectors.initCollectors(RoundMessageExpectation(commitNumber = 1), StorageMessageExpectation())

      val commitRound = system.actorOf(Props(new CommitRound()))

      within(testDuration) {
        specContext.collectors.setLogCollectorReady()
        commitRound ! StartCommit

        expectMsg(CollectorsReady)
      }
    }

    "(Prepare Round) reach consensus when 2f messages are received" in new WithActorSystem {
      val request     = new ClientRequest(Array[Byte](1))
      val specContext = new ConsensusSpecContext(self, request)

      import specContext.{consensusContext, replicaContext}

      specContext.collectors.initCollectors(RoundMessageExpectation(prepareNumber = 1), StorageMessageExpectation(isPrepare = true))

      val prepareRound = system.actorOf(Props(new PrepareRound()))

      within(testDuration) {
        prepareRound ! StartPrepare

        for (counter <- 0 until (2 * BftReplicaConfig.expectedFaultyReplicas))
          prepareRound ! Prepare(0L, specContext.sequenceNumber, specContext.view, specContext.requestDigits)

        expectMsgAllOf(FinishedPrepare, CollectorsReady)
      }
    }

    "(Commit Round) reach consensus when 2f + 1 messages are received" in new WithActorSystem {
      val request     = new ClientRequest(Array[Byte](2))
      val specContext = new ConsensusSpecContext(self, request)

      import specContext.{consensusContext, replicaContext}

      specContext.collectors.initCollectors(RoundMessageExpectation(commitNumber = 1), StorageMessageExpectation(isCommit = true, isFinish = true))

      val commitRound = system.actorOf(Props(new CommitRound()))

      within(testDuration) {
        commitRound ! StartCommit

        for (counter <- 0 until (2 * BftReplicaConfig.expectedFaultyReplicas + 1))
          commitRound ! Commit(0L, specContext.sequenceNumber, specContext.view, specContext.requestDigits)

        expectMsgAllOf(FinishedCommit, CollectorsReady)
      }
    }

    "(Prepare|Commit Round) and just ignore additional messages" in new WithActorSystem {
      val request = new ClientRequest(Array[Byte](3))
      val specContext = new ConsensusSpecContext(self, request)

      import specContext.{consensusContext, replicaContext}

      specContext.collectors.initCollectors(RoundMessageExpectation(commitNumber = 1), StorageMessageExpectation(isCommit = true, isFinish = true))

      val commitRound = system.actorOf(Props(new CommitRound()))

      within(testDuration) {
        commitRound ! StartCommit

        for (counter <- 0 until (2 * BftReplicaConfig.expectedFaultyReplicas + 1 + 1))
          commitRound ! Commit(0L, specContext.sequenceNumber, specContext.view, specContext.requestDigits)

        expectMsgAllOf(FinishedCommit, CollectorsReady)
      }
    }

    "(Prepare|Commit Round) finish directly on start if the consensus was already found" in new WithActorSystem {
      val request = new ClientRequest(Array[Byte](4))
      val specContext = new ConsensusSpecContext(self, request)

      import specContext.{consensusContext, replicaContext}

      specContext.collectors.initCollectors(RoundMessageExpectation(commitNumber = 1), StorageMessageExpectation(isCommit = true, isFinish = true))

      val commitRound = system.actorOf(Props(new CommitRound()))

      within(testDuration) {
        for (counter <- 0 until (2 * BftReplicaConfig.expectedFaultyReplicas + 1))
          commitRound ! Commit(0L, specContext.sequenceNumber, specContext.view, specContext.requestDigits)

        commitRound ! StartCommit

        expectMsgAllOf(FinishedCommit, CollectorsReady)
      }
    }
  }
}
