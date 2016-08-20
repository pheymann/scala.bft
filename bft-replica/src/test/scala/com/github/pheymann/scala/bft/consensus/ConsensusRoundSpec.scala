package com.github.pheymann.scala.bft.consensus

import akka.actor.Props
import akka.pattern.ask
import akka.util.Timeout
import com.github.pheymann.scala.bft.{BftReplicaConfig, BftReplicaSpec, WithActorSystem}
import com.github.pheymann.scala.bft.consensus.CommitRound.{Commit, FinishedCommit, StartCommit}
import com.github.pheymann.scala.bft.consensus.PrepareRound.{FinishedPrepare, Prepare, StartPrepare}
import com.github.pheymann.scala.bft.util.{ClientRequest, RoundMessageExpectation, StorageMessageExpectation}
import org.specs2.concurrent.ExecutionEnv

import scala.concurrent._
import scala.concurrent.duration._

class ConsensusRoundSpec extends BftReplicaSpec {

  sequential

  """The two different implementations of ConsensusRound are tested in this Spec.
    |
    |A consensus round""".stripMargin should {
    "(Prepare|Commit Round) distribute the current round message to all replicas on start" in new WithActorSystem {
      val request = new ClientRequest(Array[Byte](0))
      val specContext = new ConsensusSpecContext(request)

      import specContext.{consensusContext, replicaContext}

      specContext.collectors.initCollectors(RoundMessageExpectation(commitNumber = 1), StorageMessageExpectation())

      val commitRound = system.actorOf(Props(new CommitRound()))

      commitRound ! StartCommit

      Await.result(specContext.collectors.observedResult, testDuration) should beTrue
    }

    "(Prepare Round) reach consensus when 2f messages are received" in new WithActorSystem {
      val request = new ClientRequest(Array[Byte](1))
      val specContext = new ConsensusSpecContext(request)

      import specContext.{consensusContext, replicaContext}

      specContext.collectors.initCollectors(RoundMessageExpectation(prepareNumber = 1), StorageMessageExpectation(isPrepare = true))

      val prepareRound = system.actorOf(Props(new PrepareRound()))

      prepareRound ! StartPrepare

      for (counter <- 0 until (2 * BftReplicaConfig.expectedFaultyReplicas - 1))
        prepareRound ! Prepare(specContext.sequenceNumber, specContext.view, specContext.requestDigits)

      implicit val _timeout = testTimeout

      Await.result(
        prepareRound ? Prepare(specContext.sequenceNumber, specContext.view, specContext.requestDigits),
        BftReplicaConfig.timeoutDuration
      ) === FinishedPrepare

      Await.result(specContext.collectors.observedResult, testDuration) should beTrue
    }

    "(Commit Round) reach consensus when 2f + 1 messages are received" in new WithActorSystem {
      val request = new ClientRequest(Array[Byte](2))
      val specContext = new ConsensusSpecContext(request)

      import specContext.{consensusContext, replicaContext}

      specContext.collectors.initCollectors(RoundMessageExpectation(commitNumber = 1), StorageMessageExpectation(isCommit = true, isFinish = true))

      val commitRound = system.actorOf(Props(new CommitRound()))

      commitRound ! StartCommit

      for (counter <- 0 until (2 * BftReplicaConfig.expectedFaultyReplicas))
        commitRound ! Commit(specContext.sequenceNumber, specContext.view, specContext.requestDigits)

      implicit val _timeout = testTimeout

      Await.result(
        commitRound ? Commit(specContext.sequenceNumber, specContext.view, specContext.requestDigits),
        BftReplicaConfig.timeoutDuration
      ) === FinishedCommit

      Await.result(specContext.collectors.observedResult, testDuration) should beTrue
    }

    "(Prepare|Commit Round) and just ignore additional messages" in new WithActorSystem {
      val request = new ClientRequest(Array[Byte](3))
      val specContext = new ConsensusSpecContext(request)

      import specContext.{consensusContext, replicaContext}

      specContext.collectors.initCollectors(RoundMessageExpectation(commitNumber = 1), StorageMessageExpectation(isCommit = true, isFinish = true))

      val commitRound = system.actorOf(Props(new CommitRound()))

      commitRound ! StartCommit

      for (counter <- 0 until (2 * BftReplicaConfig.expectedFaultyReplicas + 1 + 3))
        commitRound ! Commit(specContext.sequenceNumber, specContext.view, specContext.requestDigits)

      Await.result(specContext.collectors.observedResult, testDuration) should beTrue
    }

    "(Prepare|Commit Round) finish directly on start if the consensus was already found" in new WithActorSystem {
      val request = new ClientRequest(Array[Byte](4))
      val specContext = new ConsensusSpecContext(request)

      import specContext.{consensusContext, replicaContext}

      specContext.collectors.initCollectors(RoundMessageExpectation(commitNumber = 1), StorageMessageExpectation(isCommit = true, isFinish = true))

      val commitRound = system.actorOf(Props(new CommitRound()))

      for (counter <- 0 until (2 * BftReplicaConfig.expectedFaultyReplicas + 1))
        commitRound ! Commit(specContext.sequenceNumber, specContext.view, specContext.requestDigits)

      commitRound ! StartCommit

      Await.result(specContext.collectors.observedResult, testDuration) should beTrue
    }

    "(Prepare|Commit Round) doesn't accept invalid messages" in new WithActorSystem {
      val request = new ClientRequest(Array[Byte](5))
      val specContext = new ConsensusSpecContext(request)

      import specContext.{consensusContext, replicaContext}

      specContext.collectors.initCollectors(RoundMessageExpectation(commitNumber = 1), StorageMessageExpectation(isCommit = true, isFinish = true))

      val commitRound = system.actorOf(Props(new CommitRound()))

      commitRound ! StartCommit

      for (counter <- 0 until (2 * BftReplicaConfig.expectedFaultyReplicas))
        commitRound ! Commit(specContext.sequenceNumber, specContext.view, specContext.requestDigits)

      commitRound ! Commit(specContext.sequenceNumber, specContext.view + 1, specContext.requestDigits)

      Await.result(specContext.collectors.observedResult, 2.seconds) should throwA[TimeoutException]
    }
  }
}
