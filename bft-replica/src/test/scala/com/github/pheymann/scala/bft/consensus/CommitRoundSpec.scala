package com.github.pheymann.scala.bft.consensus

import cats._
import com.github.pheymann.scala.bft.ScalaBftSpec
import com.github.pheymann.scala.bft.messaging.{ClientRequest, CommitMessage}
import com.github.pheymann.scala.bft.replica.{ExecuteRequest, ReplicaAction, ReplicaConfig}
import com.github.pheymann.scala.bft.storage.StoreCommit
import com.github.pheymann.scala.bft.util.ScalaBftLogger
import org.slf4j.{Logger, LoggerFactory}

class CommitRoundSpec extends ScalaBftSpec {

  implicit val specLog = LoggerFactory.getLogger(classOf[CommitRoundSpec])

  def specProcessor(implicit config: ReplicaConfig) = new (ReplicaAction ~> Id) {
    def apply[A](action: ReplicaAction[A]): Id[A] = action match {
      case validate: ValidationAction[A]  => ValidationProcessor(validate)

      case StoreCommit(_) => ()

      case ExecuteRequest(state)  => state
      case Continue(state)        => state
    }
  }

  "The commit round" should {
    """accept the commit iff it receives 2f + 1 messages. Acceptance results in execution of
      |the request and storing the message in the log""".stripMargin in {
      implicit val config = newConfig(0, 0, 1) // expect 3 messages

      val processor = specProcessor
      val state     = ConsensusState(0, 0, 0, 0, 1)

      CommitRound.processCommit(CommitMessage(1, 0, 0), state).foldMap(processor).isCommited should beFalse
      CommitRound.processCommit(CommitMessage(1, 0, 0), state).foldMap(processor).isCommited should beFalse
      CommitRound.processCommit(CommitMessage(1, 0, 0), state).foldMap(processor).isCommited should beTrue
    }

    "invalid messages should be ignored" in {
      implicit val config = newConfig(0, 0, 1)

      val state = ConsensusState(0, 0, 0, 0, 1)

      CommitRound.processCommit(CommitMessage(1, 1, 0), state).foldMap(specProcessor).receivedCommits should beEqualTo(0)
    }
  }

}
