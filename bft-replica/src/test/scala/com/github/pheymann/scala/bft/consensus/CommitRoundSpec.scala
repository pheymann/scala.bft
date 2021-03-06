package com.github.pheymann.scala.bft.consensus

import cats._
import com.github.pheymann.scala.bft.ScalaBftSpec
import com.github.pheymann.scala.bft.messaging.CommitMessage
import com.github.pheymann.scala.bft.replica.ReplicaLifting.Assign
import com.github.pheymann.scala.bft.replica.{ExecuteRequest, ReplicaAction, ReplicaConfig}
import com.github.pheymann.scala.bft.storage.StoreCommit
import org.slf4j.LoggerFactory

class CommitRoundSpec extends ScalaBftSpec {

  implicit val specLog = LoggerFactory.getLogger(classOf[CommitRoundSpec])

  def specProcessor(implicit config: ReplicaConfig) = new (ReplicaAction ~> Id) {
    def apply[A](action: ReplicaAction[A]): Id[A] = action match {
      case ValidateCommit(message, state) => MessageValidation.validateCommit(message, state)

      case StoreCommit(_) => ()

      case ExecuteRequest(state)  => state
      case Assign(value)          => value
    }
  }

  "The commit round" should {
    """accept the commit iff it receives 2f + 1 messages. Acceptance results in execution of
      |the request and storing the message in the log""".stripMargin in {
      implicit val config = newConfig(0, 0, 1) // expect 3 messages

      val processor = specProcessor
      val state     = ConsensusState(0, 0, 0, 0, 1)

      checkState(CommitRound.processCommit(CommitMessage(0, 0, 0, 0), state).foldMap(processor), "nothing")
      checkState(CommitRound.processCommit(CommitMessage(0, 0, 0, 0), state).foldMap(processor), "nothing")
      checkState(CommitRound.processCommit(CommitMessage(0, 0, 0, 0), state).foldMap(processor), "commit")
    }

    "invalid messages should be ignored" in {
      implicit val config = newConfig(0, 0, 1)

      val state = ConsensusState(0, 1, 0, 0, 1)

      CommitRound.processCommit(CommitMessage(0, 0, 1, 0), state).foldMap(specProcessor).receivedCommits should beEqualTo(0)
    }
  }

}
