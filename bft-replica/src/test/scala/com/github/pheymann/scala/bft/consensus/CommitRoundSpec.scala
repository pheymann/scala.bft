package com.github.pheymann.scala.bft.consensus

import cats._
import com.github.pheymann.scala.bft.ScalaBftSpec
import com.github.pheymann.scala.bft.messaging.{ClientRequest, CommitMessage}
import com.github.pheymann.scala.bft.replica.{ExecuteRequest, ReplicaAction, ReplicaConfig}
import com.github.pheymann.scala.bft.storage.StoreCommit
import com.github.pheymann.scala.bft.util.ScalaBftLogger
import org.slf4j.Logger

class CommitRoundSpec extends ScalaBftSpec {

  class SpecProcessor(implicit config: ReplicaConfig, log: Logger) extends (ReplicaAction ~> Id) {

    override def apply[A](action: ReplicaAction[A]): Id[A] = action match {
      case validate: ValidationAction[A]  => ValidationProcessor(validate)

      case StoreCommit(_) => ScalaBftLogger.infoLog("spec: received command - store commit")

      case ExecuteRequest(state) =>
        ScalaBftLogger.infoLog("spec: received command - execute request")
        state

      case Continue(state) => state
    }

  }

  "The commit round" should {
    """accept the commit iff it receives 2f + 1 messages. Acceptance results in execution of
      |the request and storing the message in the log""".stripMargin in new WithLogger("commit-round-spec") {
      implicit val config = newConfig(0, 0, 1) // expect 3 messages

      val processor = new SpecProcessor()
      val state     = ConsensusState(0, 0, 0, 0, 1)

      CommitRound.processCommit(CommitMessage(1, 0, 0), state).foldMap(processor).isCommited should beFalse
      CommitRound.processCommit(CommitMessage(1, 0, 0), state).foldMap(processor).isCommited should beFalse
      CommitRound.processCommit(CommitMessage(1, 0, 0), state).foldMap(processor).isCommited should beTrue
    }

    "invalid messages should be ignored" in new WithLogger("commit-round-spec") {
      implicit val config = newConfig(0, 0, 1)

      val processor = new SpecProcessor()
      val state     = ConsensusState(0, 0, 0, 0, 1)

      CommitRound.processCommit(CommitMessage(1, 1, 0), state).foldMap(processor).receivedCommits should beEqualTo(0)
    }
  }

}
