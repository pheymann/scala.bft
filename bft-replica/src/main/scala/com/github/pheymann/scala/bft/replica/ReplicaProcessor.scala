package com.github.pheymann.scala.bft.replica

import cats.{Id, ~>}
import com.github.pheymann.scala.bft.consensus._
import com.github.pheymann.scala.bft.messaging._
import com.github.pheymann.scala.bft.replica.ReplicaLifting.Assign
import com.github.pheymann.scala.bft.storage._
import org.slf4j.Logger

class ReplicaProcessor(implicit config: ReplicaConfig) extends (ReplicaAction ~> Id) {

  import ReplicaProcessor._

  override def apply[A](action: ReplicaAction[A]): Id[A] = action match {
    case ValidatePrePrepare(message, delivery, state) => MessageValidation.validatePrePrepare(message, delivery, state)
    case ValidatePrepare(message, state)  => MessageValidation.validatePrepare(message, state)
    case ValidateCommit(message, state)   => MessageValidation.validateCommit(message, state)

    case SendClientRequest(request, state) => sendClientRequest(request, state)
    case SendPrePrepareMessage(state) => sendConsensusMessage(state, prePrepareProvider)
    case SendPrepareMessage(state)    => sendConsensusMessage(state, prepareProvider)
    case SendCommitMessage(state)     => sendConsensusMessage(state, commitProvider)

    case StorePrePrepare(request, message) => ??? //TODO implemented storage
    case StorePrepare(message) => ??? //TODO implemented storage
    case StoreCommit(message)  => ??? //TODO implemented storage

    case ExecuteRequest(state) => ??? //TODO implement execute request

    case Assign(value) => value
  }

}

object ReplicaProcessor {

  def apply(implicit config: ReplicaConfig, log: Logger): ReplicaProcessor = {
    new ReplicaProcessor()
  }

  private def sendConsensusMessage(state: ConsensusState, msgProvider: (Int, Int, Int, Long) => ConsensusMessage)
                                  (implicit config: ReplicaConfig): Unit = {
    config.senderSessions.foreach {
      case (receiverId, sessionKey) =>
        val message = msgProvider(state.replicaId, receiverId, state.view, state.sequenceNumber)

        MessageSender.sendConsensusMessage(message)
    }
  }

  private def prePrepareProvider(senderId: Int, receiverId: Int, view: Int, seqNumber: Long): ConsensusMessage = {
    PrePrepareMessage(senderId, receiverId, view, seqNumber)
  }

  private def prepareProvider(senderId: Int, receiverId: Int, view: Int, seqNumber: Long): ConsensusMessage = {
    PrepareMessage(senderId, receiverId, view, seqNumber)
  }

  private def commitProvider(senderId: Int, receiverId: Int, view: Int, seqNumber: Long): ConsensusMessage = {
    CommitMessage(senderId, receiverId, view, seqNumber)
  }

  private def sendClientRequest(request: ClientRequest, state: ConsensusState)
                               (implicit config: ReplicaConfig): Unit = {
    config.senderSessions.foreach {
      case (receiverId, sessionKey) =>
        val delivery = RequestDelivery(state.replicaId, receiverId, state.view, state.sequenceNumber, request)

        MessageSender.sendClientRequest(delivery)
    }
  }

}
