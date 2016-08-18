package com.github.pheymann.scala.bft.consensus

import akka.actor.{ActorSystem, Props}
import com.github.pheymann.scala.bft.{BftReplicaConfig, BftReplicaSpec}
import com.github.pheymann.scala.bft.consensus.CommitRound.{Commit, StartCommit}
import com.github.pheymann.scala.bft.consensus.PrepareRound.{Prepare, StartPrepare}
import com.github.pheymann.scala.bft.util.{CollectorStateObserver, RoundMessageExpectation, StorageMessageExpectation}

import scala.concurrent._

class ConsensusRoundSpec extends BftReplicaSpec {

  """The two different implementations of ConsensusRound are tested in this Spec.
    |
    |A consensus round""".stripMargin should {
    "(Prepare|Commit Round) distribute the current round message to all replicas on start" in new SpecContext {
      initCollectors(RoundMessageExpectation(commitNumber = 1), StorageMessageExpectation())

      val commitRound = system.actorOf(Props(new CommitRound()))

      commitRound ! StartCommit

      observedResult(roundObserver, logObserver) should beTrue
    }

    "(Prepare Round) reach consensus when 2f messages are received" in new SpecContext {
      testSequenceNumber = 1

      initCollectors(RoundMessageExpectation(prepareNumber = 1), StorageMessageExpectation(isPrepare = true))

      val prepareRound = system.actorOf(Props(new PrepareRound()))

      prepareRound ! StartPrepare

      for (counter <- 0 until (2 * BftReplicaConfig.expectedFaultyReplicas))
        prepareRound ! Prepare(testSequenceNumber, testView, testRequestDigits)

      observedResult(roundObserver, logObserver) should beTrue
    }

    "(Commit Round) reach consensus when 2f + 1 messages are received" in new SpecContext {
      testSequenceNumber = 2

      initCollectors(RoundMessageExpectation(commitNumber = 1), StorageMessageExpectation(isCommit = true, isFinish = true))

      val commitRound = system.actorOf(Props(new CommitRound()))

      commitRound ! StartCommit

      for (counter <- 0 until (2 * BftReplicaConfig.expectedFaultyReplicas + 1))
        commitRound ! Commit(testSequenceNumber, testView, testRequestDigits)

      observedResult(roundObserver, logObserver) should beTrue
    }

    "(Prepare|Commit Round) and just ignore additional messages" in new SpecContext {
      testSequenceNumber = 3

      initCollectors(RoundMessageExpectation(commitNumber = 1), StorageMessageExpectation(isCommit = true, isFinish = true))

      val commitRound = system.actorOf(Props(new CommitRound()))

      commitRound ! StartCommit

      for (counter <- 0 until (2 * BftReplicaConfig.expectedFaultyReplicas + 1 + 3))
        commitRound ! Commit(testSequenceNumber, testView, testRequestDigits)

      observedResult(roundObserver, logObserver) should beTrue
    }

    "(Prepare|Commit Round) finish directly on start if the consensus was already found" in new SpecContext {
      testSequenceNumber = 4

      initCollectors(RoundMessageExpectation(commitNumber = 1), StorageMessageExpectation(isCommit = true, isFinish = true))

      val commitRound = system.actorOf(Props(new CommitRound()))

      for (counter <- 0 until (2 * BftReplicaConfig.expectedFaultyReplicas + 1))
        commitRound ! Commit(testSequenceNumber, testView, testRequestDigits)

      commitRound ! StartCommit

      observedResult(roundObserver, logObserver) should beTrue
    }

    "(Prepare|Commit Round) doesn't accept invalid messages" in new SpecContext {
      testSequenceNumber = 5

      initCollectors(RoundMessageExpectation(commitNumber = 1), StorageMessageExpectation(isCommit = true, isFinish = true))

      val commitRound = system.actorOf(Props(new CommitRound()))

      commitRound ! StartCommit

      for (counter <- 0 until (2 * BftReplicaConfig.expectedFaultyReplicas))
        commitRound ! Commit(testSequenceNumber, testView, testRequestDigits)

      commitRound ! Commit(testSequenceNumber, testView + 1, testRequestDigits)

      observedResult(roundObserver, logObserver) should throwA[TimeoutException]
    }
  }

  // when returning the expectations are fulfilled in the collectors
  private def observedResult(roundObserver: CollectorStateObserver, logObserver: CollectorStateObserver)
                            (implicit system: ActorSystem) = {
    import system.dispatcher

    val result = for {
      roundValid  <- roundObserver.checkCollector
      logValid    <- logObserver.checkCollector
    } yield roundValid && logValid

    Await.result(result, timeoutDuration)
  }

}
