package com.github.pheymann.scala.bft.consensus

import cats._
import com.github.pheymann.scala.bft.ScalaBftSpec
import com.github.pheymann.scala.bft.messaging.{ClientRequest, PrepareMessage}
import com.github.pheymann.scala.bft.replica.{ReplicaAction, ReplicaConfig}
import com.github.pheymann.scala.bft.storage.StorePrepare
import com.github.pheymann.scala.bft.util.ScalaBftLogger
import org.slf4j.Logger

class PrepareRoundSpec extends ScalaBftSpec {

  class SpecProcessor(implicit config: ReplicaConfig, log: Logger) extends (ReplicaAction ~> Id) {

    override def apply[A](action: ReplicaAction[A]): Id[A] = action match {
      case validate: ValidationAction[A]  => ValidationProcessor(validate)

      case StorePrepare(_)          => ScalaBftLogger.infoLog("spec: received command - store commit")
      case SendCommitMessage(state) => ScalaBftLogger.infoLog("spec: received command - send commit message")

      case Continue(state) => state
    }

  }

  "The prepare round" should {
    """accepts the prepare iff it receives 2f messages. Acceptance results in transmission of
      |a commit message and storing the prepare message in the log""".stripMargin in new WithLogger("prepare-round-spec") {
      implicit val config = newConfig(0, 0, 1) // expect 2 messages

      val processor = new SpecProcessor()
      val state     = ConsensusState(0, 0, 0, 0, 1)

      PrepareRound.processPrepare(PrepareMessage(1, 0, 0), state).foldMap(processor).isPrepared should beFalse
      PrepareRound.processPrepare(PrepareMessage(1, 0, 0), state).foldMap(processor).isPrepared should beTrue
    }

    "invalid messages should be ignored" in new WithLogger("prepare-round-spec") {
      implicit val config = newConfig(0, 0, 1)

      val processor = new SpecProcessor()
      val state     = ConsensusState(0, 0, 0, 0, 1)

      PrepareRound.processPrepare(PrepareMessage(1, 1, 0), state).foldMap(processor).receivedPrepares should beEqualTo(0)
    }
  }

}
