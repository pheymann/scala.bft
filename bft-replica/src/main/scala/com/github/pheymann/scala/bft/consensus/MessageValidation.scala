package com.github.pheymann.scala.bft.consensus

import cats.free.Free
import com.github.pheymann.scala.bft.messaging._
import com.github.pheymann.scala.bft.replica.{ReplicaContext, ServiceAction}
import com.github.pheymann.scala.bft.util.ScalaBftLogger

object MessageValidation {

  import com.github.pheymann.scala.bft.storage.StorageAction._

  private[consensus] def validateMessage(message: ConsensusMessage)
                                        (implicit context: ReplicaContext): Boolean = {
    import context.config

    message.receiverId  == context.config.id &&
      message.view      == context.view &&
      message.sequenceNumber >= config.lowWatermark &&
      message.sequenceNumber <= config.highWatermark
  }

  def validatePrePrepare(
                          message:  PrePrepareMessage,
                          delivery: RequestDelivery,
                          state:    ConsensusState
                        )(implicit context: ReplicaContext): Free[ServiceAction, (Boolean, ConsensusState)] = {
    import state.log

    val validateMessageAndRequest = {
      message.senderId    == delivery.senderId &&
      message.receiverId  == delivery.receiverId &&
      message.view        == delivery.view &&
      message.sequenceNumber == delivery.sequenceNumber
    }

    if (validateMessageAndRequest && validateMessage(message)) {
      isPrePrepareStored(message).map { isAlreadyStored =>
        if (!isAlreadyStored) {
          ScalaBftLogger.logInfo(s"${message.toLog}.pre-prepare.consent")
          state.isPrePrepared = true

          (true, state)
        }
        else {
          ScalaBftLogger.logWarn(s"${message.toLog}.pre-prepare.invalid")
          (false, state)
        }
      }
    }
    else{
      ScalaBftLogger.logWarn(s"${message.toLog}.pre-prepare.invalid")
      Free.pure((false, state))
    }
  }

  def validatePrepare(message: PrepareMessage, state: ConsensusState)
                     (implicit context: ReplicaContext): Free[ServiceAction, (Boolean, ConsensusState)] = {
    import state.log

    if (validateMessage(message)) {
      isPrepareStored(message).map { isAlreadyStored =>
        if (!isAlreadyStored) {
          state.receivedPrepares += 1

          if (state.receivedPrepares == context.config.expectedPrepares) {
            ScalaBftLogger.logInfo(s"${message.toLog}.prepare.consent")
            state.isPrepared = true
          }
          (true, state)
        }
        else {
          ScalaBftLogger.logDebug("prepare.invalid")
          (false, state)
        }
      }
    }
    else {
      ScalaBftLogger.logDebug("prepare.invalid")
      Free.pure((false, state))
    }
  }

  def validateCommit(message: CommitMessage, state: ConsensusState)
                    (implicit context: ReplicaContext): Free[ServiceAction, (Boolean, ConsensusState)] = {
    import state.log

    if (validateMessage(message)) {
      isCommitStored(message).map { isAlreadyStored =>
        if (!isAlreadyStored) {
          state.receivedCommits += 1

          if (state.receivedCommits == context.config.expectedCommits) {
            ScalaBftLogger.logInfo(s"${message.toLog}.commit.consent")
            state.isCommited = true
          }
          (true, state)
        }
        else {
          ScalaBftLogger.logDebug("commit.invalid")
          (false, state)
        }
      }
    }
    else {
      ScalaBftLogger.logDebug("commit.invalid")
      Free.pure((false, state))
    }
  }

}
