package com.github.pheymann.scala.bft.consensus

import cats._
import com.github.pheymann.scala.bft.ScalaBftSpec
import com.github.pheymann.scala.bft.messaging.{ClientRequest, PrePrepareMessage, RequestDelivery}
import com.github.pheymann.scala.bft.replica.ReplicaAction
import com.github.pheymann.scala.bft.replica.ReplicaLifting.Assign
import com.github.pheymann.scala.bft.storage.StorePrePrepare
import org.slf4j.LoggerFactory

class PrePrepareRoundSpec extends ScalaBftSpec {

  implicit val specLog = LoggerFactory.getLogger(classOf[PrePrepareRoundSpec])

  val specProcessor = new (ReplicaAction ~> Id) {
    def apply[A](action: ReplicaAction[A]): Id[A] = action match {
      case ValidatePrePrepare(message, delivery, state) => MessageValidation.validatePrePrepare(message, delivery, state)

      case SendPrePrepareMessage(_) => ()
      case SendPrepareMessage(_)    => ()
      case SendClientRequest(_, _)  => ()

      case StorePrePrepare(_, _) => ()

      case Assign(value) => value
    }
  }

  "The pre-prepare round" should {
    """send a pre-prepare message, ClientRequest and afterwards the prepare message to all replicas if
      |it is the leader and store them also in the log
    """.stripMargin in {
      val state     = ConsensusState(0, 0, 0, 0, 1)
      val request   = ClientRequest(0, 0L, Array.empty)

      checkState(PrePrepareRound.processLeaderPrePrepare(request, state).foldMap(specProcessor), "pre-prepare")
    }

    "send a prepare message to all other replicas if it receives a valid pre-prepare and request from the leader" in {
      val message   = PrePrepareMessage(0, 0, 0, 0L)
      val delivery  = RequestDelivery(0, 0, 0, 0L, ClientRequest(0, 0L, Array.empty))
      val state     = ConsensusState(0, 0, 0, 0, 1)

      // valid message / request pair
      checkState(
        PrePrepareRound.processFollowerPrePrepare(message, delivery, state).foldMap(specProcessor),
        "pre-prepare"
      )

      // invalid message / request pair
      val invalidMessage = PrePrepareMessage(1, 0, 0, 0L)
      val invalidState   = ConsensusState(0, 0, 0, 0, 1)

      checkState(
        PrePrepareRound.processFollowerPrePrepare(invalidMessage, delivery, invalidState).foldMap(specProcessor),
        "nothing"
      )
    }
  }

}
