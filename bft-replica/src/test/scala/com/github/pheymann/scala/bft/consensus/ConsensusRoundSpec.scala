package com.github.pheymann.scala.bft.consensus

import akka.actor.Props
import com.github.pheymann.scala.bft.{BftReplicaConfig, BftReplicaSpec, WithActorSystem}
import com.github.pheymann.scala.bft.consensus.CommitRound.{Commit, FinishedCommit, StartCommit}
import com.github.pheymann.scala.bft.consensus.PrepareRound.{FinishedPrepare, Prepare, StartPrepare}
import com.github.pheymann.scala.bft.replica.ReplicasMock.CalledSendMessage
import com.github.pheymann.scala.bft.storage.LogStorageMock.{CalledAddCommit, CalledAddPrepare, CalledFinish}
import com.github.pheymann.scala.bft.util.ClientRequest

class ConsensusRoundSpec extends BftReplicaSpec {

  sequential

  """The two different implementations of ConsensusRound are tested in this Spec.
    |
    |A consensus round""".stripMargin should {
    "(Prepare|Commit Round) distribute the current round message to all replicas on start" in new WithActorSystem {
      val request     = new ClientRequest(0, 0, Array[Byte](0))
      val specContext = new ConsensusSpecContext(self, request)

      import specContext.{consensusContext, replicaContext}

      val commitRound = system.actorOf(Props(new CommitRound()))

      within(testDuration) {
        commitRound ! StartCommit

        expectMsg(CalledSendMessage)
      }
    }

    "(Prepare Round) reach consensus when 2f messages are received" in new WithActorSystem {
      val request     = new ClientRequest(0, 0, Array[Byte](1))
      val specContext = new ConsensusSpecContext(self, request)

      import specContext.{consensusContext, replicaContext}

      val prepareRound = system.actorOf(Props(new PrepareRound()))

      within(testDuration) {
        prepareRound ! StartPrepare

        for (counter <- 0 until (2 * BftReplicaConfig.expectedFaultyReplicas))
          prepareRound ! Prepare(0L, specContext.sequenceNumber, specContext.view, specContext.requestDigits)

        expectMsg(CalledSendMessage)
        expectMsg(CalledAddPrepare)
        expectMsg(FinishedPrepare)
      }
    }

    "(Commit Round) reach consensus when 2f + 1 messages are received" in new WithActorSystem {
      val request     = new ClientRequest(0, 0, Array[Byte](2))
      val specContext = new ConsensusSpecContext(self, request)

      import specContext.{consensusContext, replicaContext}

      val commitRound = system.actorOf(Props(new CommitRound()))

      within(testDuration) {
        commitRound ! StartCommit

        for (counter <- 0 until (2 * BftReplicaConfig.expectedFaultyReplicas + 1))
          commitRound ! Commit(0L, specContext.sequenceNumber, specContext.view, specContext.requestDigits)

        expectMsg(CalledSendMessage)
        expectMsg(CalledAddCommit)
        expectMsg(CalledFinish)
        expectMsg(FinishedCommit)
      }
    }

    "(Prepare|Commit Round) and just ignore additional messages" in new WithActorSystem {
      val request     = new ClientRequest(0, 0, Array[Byte](3))
      val specContext = new ConsensusSpecContext(self, request)

      import specContext.{consensusContext, replicaContext}

      val commitRound = system.actorOf(Props(new CommitRound()))

      within(testDuration) {
        commitRound ! StartCommit

        for (counter <- 0 until (2 * BftReplicaConfig.expectedFaultyReplicas + 1 + 1))
          commitRound ! Commit(0L, specContext.sequenceNumber, specContext.view, specContext.requestDigits)

        expectMsg(CalledSendMessage)
        expectMsg(CalledAddCommit)
        expectMsg(CalledFinish)
        expectMsg(FinishedCommit)
      }
    }

    "(Prepare|Commit Round) finish directly on start if the consensus was already found" in new WithActorSystem {
      val request     = new ClientRequest(0, 0, Array[Byte](4))
      val specContext = new ConsensusSpecContext(self, request)

      import specContext.{consensusContext, replicaContext}

      val commitRound = system.actorOf(Props(new CommitRound()))

      within(testDuration) {
        for (counter <- 0 until (2 * BftReplicaConfig.expectedFaultyReplicas + 1))
          commitRound ! Commit(0L, specContext.sequenceNumber, specContext.view, specContext.requestDigits)

        commitRound ! StartCommit

        expectMsg(CalledSendMessage)
        expectMsg(CalledAddCommit)
        expectMsg(CalledFinish)
        expectMsg(FinishedCommit)
      }
    }
  }
}
