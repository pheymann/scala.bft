package com.github.pheymann.scala.bft.consensus

import cats._
import com.github.pheymann.scala.bft.ScalaBftSpec
import com.github.pheymann.scala.bft.messaging.MessagingAction.BroadcastCommit
import com.github.pheymann.scala.bft.messaging.PrepareMessage
import com.github.pheymann.scala.bft.replica.ServiceAction.EmptyAction
import com.github.pheymann.scala.bft.replica.ServiceAction
import com.github.pheymann.scala.bft.storage.StorageAction.{IsPrepareStored, StorePrepare}
import org.slf4j.LoggerFactory

class PrepareRoundSpec extends ScalaBftSpec {

  implicit val specLog = LoggerFactory.getLogger(classOf[PrepareRoundSpec])

  val interpreter = (isStored: Boolean) => new (ServiceAction ~> Id) {
    def apply[A](action: ServiceAction[A]): Id[A] = action match {
      case StorePrepare(_)    => ()
      case IsPrepareStored(_) => isStored
      case BroadcastCommit    => ()
      case EmptyAction        => ()
    }
  }

  "The prepare round" should {
    """accepts the prepare iff it receives 2f messages. Acceptance results in transmission of
      |a commit message and storing the prepare message in the log""".stripMargin in {
      implicit val context = newContext(false, 0, 1) // expect 2 messages

      val state = ConsensusState(0, 0, 0L)

      checkState(
        PrepareRound.processPrepare(PrepareMessage(0, 0, 0, 0), state).foldMap(interpreter(false)),
        "nothing"
      )
      checkState(
        PrepareRound.processPrepare(PrepareMessage(0, 0, 0, 0), state).foldMap(interpreter(false)),
        "prepare"
      )
    }

    "invalid or already stored messages should be ignored" in {
      implicit val context = newContext(false, 0, 1)

      val state = ConsensusState(0, 0, 0L)

      PrepareRound.processPrepare(PrepareMessage(0, 0, 1, 0), state).foldMap(interpreter(false)).receivedPrepares should beEqualTo(0)
      PrepareRound.processPrepare(PrepareMessage(0, 0, 0, 0), state).foldMap(interpreter(true)).receivedPrepares should beEqualTo(0)
    }
  }

}
