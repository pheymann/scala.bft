package com.github.pheymann.scala.bft.consensus

import cats._
import com.github.pheymann.scala.bft.ScalaBftSpec
import com.github.pheymann.scala.bft.consensus.ValidationAction.ValidatePrePrepare
import com.github.pheymann.scala.bft.messaging.MessagingAction.{BroadcastPrePrepare, BroadcastPrepare, BroadcastRequest}
import com.github.pheymann.scala.bft.messaging.{ClientRequest, PrePrepareMessage, RequestDelivery}
import com.github.pheymann.scala.bft.replica.ServiceAction
import com.github.pheymann.scala.bft.replica.ServiceAction.EmptyAction
import com.github.pheymann.scala.bft.storage.StorageAction.StorePrePrepare
import org.slf4j.LoggerFactory

class PrePrepareRoundSpec extends ScalaBftSpec {

  implicit val specLog = LoggerFactory.getLogger(classOf[PrePrepareRoundSpec])

  val specProcessor = new (ServiceAction ~> Id) {
    def apply[A](action: ServiceAction[A]): Id[A] = action match {
      case ValidatePrePrepare(message, delivery, state) => MessageValidation.validatePrePrepare(message, delivery, state)

      case StorePrePrepare(_, _)  => ()
      case BroadcastPrePrepare    => ()
      case BroadcastRequest(_)    => ()
      case BroadcastPrepare       => ()
      case EmptyAction            => ()
    }
  }

  "The pre-prepare round" should {
    """send a pre-prepare message, ClientRequest and afterwards the prepare message to all replicas if
      |it is the leader and store them also in the log
    """.stripMargin in {
      val state     = ConsensusState(0, 0, 0L)
      val request   = ClientRequest(0, 0L, Array.empty)

      checkState(PrePrepareRound.processLeaderPrePrepare(request, state).foldMap(specProcessor), "pre-prepare")
    }

    "send a prepare message to all other replicas if it receives a valid pre-prepare and request from the leader" in {
      val message   = PrePrepareMessage(0, 0, 0, 0L)
      val delivery  = RequestDelivery(0, 0, 0, 0L, ClientRequest(0, 0L, Array.empty))
      val state     = ConsensusState(0, 0, 0L)

      // valid message / request pair
      checkState(
        PrePrepareRound.processFollowerPrePrepare(message, delivery, state).foldMap(specProcessor),
        "pre-prepare"
      )

      // invalid message / request pair
      val invalidMessage = PrePrepareMessage(1, 0, 0, 0L)
      val invalidState   = ConsensusState(0, 0, 0L)

      checkState(
        PrePrepareRound.processFollowerPrePrepare(invalidMessage, delivery, invalidState).foldMap(specProcessor),
        "nothing"
      )
    }
  }

}
