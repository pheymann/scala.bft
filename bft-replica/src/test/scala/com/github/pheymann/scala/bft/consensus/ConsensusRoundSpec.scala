package com.github.pheymann.scala.bft.consensus

import akka.actor.Props
import com.github.pheymann.scala.bft.BftReplicaSpec
import com.github.pheymann.scala.bft.consensus.CommitRound.StartCommit
import com.github.pheymann.scala.bft.storage.LogStorageMock
import com.github.pheymann.scala.bft.util.{RoundMessageExpectation, StorageMessageExpectation}

import scala.concurrent.Await

class ConsensusRoundSpec extends BftReplicaSpec {

  """The two different implementations of ConsensusRound are tested in this Spec.
    |
    |A consensus round""".stripMargin should {
    "should distribute the current round message to all replicas on start" in new SpecContext {
      import system.dispatcher

      val logStorageMock = new LogStorageMock {
        val _logCollectorRef = logCollectorRef

        override def isWithinWatermarks(message: ConsensusMessage) = true
        override def hasAccepted(message: ConsensusMessage) = false
      }

      initCollectors(RoundMessageExpectation(commitNumber = 1), StorageMessageExpectation())

      val commitRound = system.actorOf(Props(new CommitRound()))

      commitRound ! StartCommit

      // when returning the expectations are fulfilled in the collectors
      val result = for {
        roundValid  <- roundObserver.checkCollector
        logValid    <- logObserver.checkCollector
      } yield roundValid && logValid

      Await.result(result, timeoutDuration) should beTrue
    }
  }

}
