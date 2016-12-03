package com.github.pheymann.scala.bft.consensus

import com.github.pheymann.scala.bft.ScalaBftSpec
import com.github.pheymann.scala.bft.messaging._
import org.slf4j.LoggerFactory
import MessageValidation._
import cats.free.Free
import cats.{Id, ~>}
import com.github.pheymann.scala.bft.replica.ServiceAction
import com.github.pheymann.scala.bft.storage.StorageAction.{IsCommitStored, IsPrePrepareStored, IsPrepareStored}

class MessageValidationSpec extends ScalaBftSpec {

  implicit val specLog = LoggerFactory.getLogger(classOf[MessageValidationSpec])

  val interpreter = (isStored: Boolean) => new (ServiceAction ~> Id) {
    def apply[R](action: ServiceAction[R]): Id[R] = action match {
      case IsPrePrepareStored(_) | IsPrepareStored(_) | IsCommitStored(_) => isStored
    }
  }

  "Received consensus messages" should {
    """have the same view as the receiver replica, a sequence number within
      |defined watermarks and the expected sender/receiver ids""".stripMargin in {
      implicit val context = newContext(false, 0, 1)

      // valid messages
      validateMessage(CommitMessage(0, 0, 0, 0)) should beTrue
      validateMessage(CommitMessage(0, 0, 0, 1)) should beTrue

      // invalid messages
      validateMessage(CommitMessage(0, 0, 0, 3)) should beFalse
      validateMessage(CommitMessage(0, 0, 1, 0)) should beFalse
      validateMessage(CommitMessage(0, 1, 1, 0)) should beFalse
    }

    "set the state to pre-prepared := true if the message is valid" in {
      implicit val context = newContext(false, 0, 1)

      val state = ConsensusState(0, 0, 0L)

      val request   = ClientRequest(0, 0L, Array.empty)
      val delivery  = RequestDelivery(0, 0, 0, 0L, request)

      def checkState(result: Free[ServiceAction, (Boolean, ConsensusState)], isStored: Boolean)
                    (isValid: Boolean, isPrePrepared: Boolean) = {
        val (_isValid, state) = result.foldMap(interpreter(isStored))

        _isValid should beEqualTo(isValid)
        state.isPrePrepared should beEqualTo(isPrePrepared)
      }

      //ignored: invalid message
      checkState(validatePrePrepare(PrePrepareMessage(1, 0, 0, 0L), delivery, state), false)(false, false)
      checkState(validatePrePrepare(PrePrepareMessage(0, 1, 0, 0L), delivery, state), false)(false, false)
      checkState(validatePrePrepare(PrePrepareMessage(0, 0, 1, 0L), delivery, state), false)(false, false)
      checkState(validatePrePrepare(PrePrepareMessage(0, 0, 0, 1L), delivery, state), false)(false, false)

      //ignored: already
      checkState(validatePrePrepare(PrePrepareMessage(0, 0, 0, 0L), delivery, state), true)(false, false)

      //accepted
      checkState(validatePrePrepare(PrePrepareMessage(0, 0, 0, 0L), delivery, state), false)(true, true)
    }

    "set the state to prepared := true if 2f messages are received" in {
      implicit val context = newContext(false, 0, 1)

      val state = ConsensusState(0, 0, 0L)

      def checkState(result: Free[ServiceAction, (Boolean, ConsensusState)], isStored: Boolean)
                    (isValid: Boolean, isConsensus: Boolean, receivedMessages: Int) = {
        val (_isValid, state) = result.foldMap(interpreter(isStored))

        _isValid should beEqualTo(isValid)
        state.isPrepared should beEqualTo(isConsensus)
        state.receivedPrepares should beEqualTo(receivedMessages)
      }

      //ignored: invalid message
      checkState(validatePrepare(PrepareMessage(0, 0, 1, 0), state), false)(false, false, 0)

      //ignored: already stored message
      checkState(validatePrepare(PrepareMessage(0, 0, 0, 0), state), true)(false, false, 0)

      //accepted
      checkState(validatePrepare(PrepareMessage(0, 0, 0, 0), state), false)(true, false, 1)
      checkState(validatePrepare(PrepareMessage(0, 0, 0, 0), state), false)(true, true, 2)
    }

    "set the state to commited := true if 2f + 1 messages are received" in {
      implicit val context = newContext(false, 0, 1)

      val state = ConsensusState(0, 0, 0L)

      def checkState(result: Free[ServiceAction, (Boolean, ConsensusState)], isStored: Boolean)
                    (isValid: Boolean, isConsensus: Boolean, receivedMessages: Int) = {
        val (_isValid, state) = result.foldMap(interpreter(isStored))

        _isValid should beEqualTo(isValid)
        state.isCommited should beEqualTo(isConsensus)
        state.receivedCommits should beEqualTo(receivedMessages)
      }

      //ignored: invalid message
      checkState(validateCommit(CommitMessage(0, 0, 1, 0), state), false)(false, false, 0)

      //ignored: already stored message
      checkState(validateCommit(CommitMessage(0, 0, 0, 0), state), true)(false, false, 0)

      //accepted
      checkState(validateCommit(CommitMessage(0, 0, 0, 0), state), false)(true, false, 1)
      checkState(validateCommit(CommitMessage(0, 0, 0, 0), state), false)(true, false, 2)
      checkState(validateCommit(CommitMessage(0, 0, 0, 0), state), false)(true, true, 3)
    }
  }

}
