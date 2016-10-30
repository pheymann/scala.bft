package com.github.pheymann.scala.bft.consensus

import com.github.pheymann.scala.bft.messaging.{ConsensusMessage, PrePrepareMessage, PrepareMessage, RequestDelivery}
import com.github.pheymann.scala.bft.replica.ReplicaConfig
import com.github.pheymann.scala.bft.util.ScalaBftLogger
import org.slf4j.Logger

object MessageValidation {

  private[consensus] def validateMessage(message: ConsensusMessage, state: ConsensusState)
                                        (implicit config: ReplicaConfig): Boolean = {
    message.receiverId  == state.replicaId &&
    message.view        == config.view &&
    message.sequenceNumber >= state.lowWatermark &&
    message.sequenceNumber <= state.highWatermark
  }

  def validatePrePrepare(
                          message:  PrePrepareMessage,
                          delivery: RequestDelivery,
                          state:    ConsensusState
                        )(implicit log: Logger): ConsensusState = {
    if (
      message.senderId    == delivery.senderId &&
      message.receiverId  == delivery.receiverId &&
      message.view        == delivery.view &&
      message.sequenceNumber == delivery.sequenceNumber
    ) {
      ScalaBftLogger.infoLog(s"${message.toLog}.pre-prepare.consent")
      state.isPrePrepared = true
    }
    else
      ScalaBftLogger.warnLog(s"${message.toLog}.pre-prepare.invalid")

    state
  }

  def validatePrepare(message: PrepareMessage, state: ConsensusState)
                     (implicit config: ReplicaConfig, log: Logger): ConsensusState = {
    if (validateMessage(message, state)) {
      state.receivedPrepares += 1

      if (state.receivedPrepares == config.expectedPrepares) {
        ScalaBftLogger.infoLog(s"${message.toLog}.prepare.consent")
        state.isPrepared = true
      }
    }
    else
      ScalaBftLogger.debugLog("prepare.invalid")

    state
  }

  def validateCommit(message: ConsensusMessage, state: ConsensusState)
                    (implicit config: ReplicaConfig, log: Logger): ConsensusState = {
    if (validateMessage(message, state)) {
      state.receivedCommits += 1

      if (state.receivedCommits == config.expectedCommits) {
        ScalaBftLogger.infoLog(s"${message.toLog}.commit.consent")
        state.isCommited = true
      }
    }
    else
      ScalaBftLogger.debugLog("commit.invalid")

    state
  }

}
