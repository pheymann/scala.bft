package com.github.pheymann.scala.bft.consensus

import com.github.pheymann.scala.bft.ScalaBftSpec
import com.github.pheymann.scala.bft.messaging.{CommitMessage, PrepareMessage}
import com.github.pheymann.scala.bft.replica.ReplicaConfig

class MessageValidationSpec extends ScalaBftSpec {

  "Received consensus messages" should {
    """have the same view as the receiver replica and a sequence number within
      |defined watermarks""".stripMargin in new WithLogger("message-validation-spec") {
      implicit val config = newConfig(0, 0, 1)

      val state = ConsensusState(0, 0, 0, 0, 1)

      // valid messages
      MessageValidation.validateMessage(CommitMessage(1, 0, 0), state) should beTrue
      MessageValidation.validateMessage(CommitMessage(1, 0, 1), state) should beTrue

      // invalid messages
      MessageValidation.validateMessage(CommitMessage(1, 0, 3), state) should beFalse
      MessageValidation.validateMessage(CommitMessage(1, 1, 0), state) should beFalse
    }

    "set the state to prepared := true if 2f messages are received" in new WithLogger("message-validation-spec") {
      implicit val config = newConfig(0, 0, 1)

      val state = ConsensusState(0, 0, 0, 0, 1)

      def checkState(state: ConsensusState)(isConsensus: Boolean, receivedMessages: Int) = {
        state.isPrepared should beEqualTo(isConsensus)
        state.receivedPrepares should beEqualTo(receivedMessages)
      }

      //ignored
      checkState(MessageValidation.validatePrepare(PrepareMessage(1, 1, 0), state))(false, 0)

      //accepted
      checkState(MessageValidation.validatePrepare(PrepareMessage(1, 0, 0), state))(false, 1)
      checkState(MessageValidation.validatePrepare(PrepareMessage(1, 0, 0), state))(true, 2)
    }

    "set the state to commited := true if 2f + 1 messages are received" in new WithLogger("message-validation-spec") {
      implicit val config = newConfig(0, 0, 1)

      val state = ConsensusState(0, 0, 0, 0, 1)

      def checkState(state: ConsensusState)(isConsensus: Boolean, receivedMessages: Int) = {
        state.isCommited should beEqualTo(isConsensus)
        state.receivedCommits should beEqualTo(receivedMessages)
      }

      //ignored
      checkState(MessageValidation.validatePrepare(CommitMessage(1, 1, 0), state))(false, 0)

      //accepted
      checkState(MessageValidation.validateCommit(CommitMessage(1, 0, 0), state))(false, 1)
      checkState(MessageValidation.validateCommit(CommitMessage(1, 0, 0), state))(false, 2)
      checkState(MessageValidation.validateCommit(CommitMessage(1, 0, 0), state))(true, 3)
    }
  }

}
