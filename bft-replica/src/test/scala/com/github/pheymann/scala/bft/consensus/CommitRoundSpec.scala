package com.github.pheymann.scala.bft.consensus

import cats._
import com.github.pheymann.scala.bft.ScalaBftSpec
import com.github.pheymann.scala.bft.messaging.CommitMessage
import com.github.pheymann.scala.bft.replica.ServiceAction.EmptyAction
import com.github.pheymann.scala.bft.replica.ServiceAction
import com.github.pheymann.scala.bft.storage.StorageAction.{IsCommitStored, StoreCommit}
import org.slf4j.LoggerFactory

class CommitRoundSpec extends ScalaBftSpec {

  implicit val specLog = LoggerFactory.getLogger(classOf[CommitRoundSpec])

  val interpreter = (isStored: Boolean) => new (ServiceAction ~> Id) {
    def apply[A](action: ServiceAction[A]): Id[A] = action match {
      case StoreCommit(_)     => ()
      case IsCommitStored(_)  => isStored
      case EmptyAction        => ()
    }
  }

  "The commit round" should {
    """accept the commit iff it receives 2f + 1 messages. Acceptance results in execution of
      |the request and storing the message in the log""".stripMargin in {
      implicit val config = newContext(false, 0, 1) // expect 3 messages

      val state = ConsensusState(0, 0, 0L)

      checkState(CommitRound.processCommit(CommitMessage(0, 0, 0, 0), state).foldMap(interpreter(false)), "nothing")
      checkState(CommitRound.processCommit(CommitMessage(0, 0, 0, 0), state).foldMap(interpreter(false)), "nothing")
      checkState(CommitRound.processCommit(CommitMessage(0, 0, 0, 0), state).foldMap(interpreter(false)), "commit")
    }

    "invalid or already stored messages should be ignored" in {
      implicit val config = newContext(false, 0, 1)

      val state = ConsensusState(0, 1, 0L)

      CommitRound.processCommit(CommitMessage(0, 0, 1, 0), state).foldMap(interpreter(false)).receivedCommits should beEqualTo(0)
      CommitRound.processCommit(CommitMessage(0, 0, 0, 0), state).foldMap(interpreter(true)).receivedCommits should beEqualTo(0)
    }
  }

}
