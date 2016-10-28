package com.github.pheymann.scala.bft.consensus

import cats._
import com.github.pheymann.scala.bft.ScalaBftSpec
import com.github.pheymann.scala.bft.messaging.PrepareMessage
import com.github.pheymann.scala.bft.replica.ReplicaLifting.Assign
import com.github.pheymann.scala.bft.replica.{ReplicaAction, ReplicaConfig}
import com.github.pheymann.scala.bft.storage.StorePrepare
import org.slf4j.LoggerFactory

class PrepareRoundSpec extends ScalaBftSpec {

  implicit val specLog = LoggerFactory.getLogger(classOf[PrepareRoundSpec])

  def specProcessor(implicit config: ReplicaConfig) = new (ReplicaAction ~> Id) {
    def apply[A](action: ReplicaAction[A]): Id[A] = action match {
      case ValidatePrepare(message, state) => MessageValidation.validatePrepare(message, state)

      case StorePrepare(_)          => ()
      case SendCommitMessage(state) => ()

      case Assign(value) => value
    }
  }

  "The prepare round" should {
    """accepts the prepare iff it receives 2f messages. Acceptance results in transmission of
      |a commit message and storing the prepare message in the log""".stripMargin in {
      implicit val config = newConfig(0, 0, 1) // expect 2 messages

      val processor = specProcessor
      val state     = ConsensusState(0, 0, 0, 0, 1)

      PrepareRound.processPrepare(PrepareMessage(1, 0, 0), state).foldMap(processor).isPrepared should beFalse
      PrepareRound.processPrepare(PrepareMessage(1, 0, 0), state).foldMap(processor).isPrepared should beTrue
    }

    "invalid messages should be ignored" in {
      implicit val config = newConfig(0, 0, 1)

      val state = ConsensusState(0, 0, 0, 0, 1)

      PrepareRound.processPrepare(PrepareMessage(1, 1, 0), state).foldMap(specProcessor).receivedPrepares should beEqualTo(0)
    }
  }

}
