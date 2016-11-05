package com.github.pheymann.scala.bft.replica

import cats.{Id, ~>}
import com.github.pheymann.scala.bft.consensus._
import com.github.pheymann.scala.bft.messaging.SenderActor._
import com.github.pheymann.scala.bft.messaging._
import com.github.pheymann.scala.bft.replica.ReplicaLifting.Assign
import com.github.pheymann.scala.bft.storage._
import org.slf4j.Logger

class ReplicaProcessor(implicit config: ReplicaConfig) extends (ReplicaAction ~> Id) {

  import ReplicaProcessor._
  import Send._

  override def apply[A](action: ReplicaAction[A]): Id[A] = action match {
    case ValidatePrePrepare(message, delivery, state) => MessageValidation.validatePrePrepare(message, delivery, state)
    case ValidatePrepare(message, state)  => MessageValidation.validatePrepare(message, state)
    case ValidateCommit(message, state)   => MessageValidation.validateCommit(message, state)

    case SendClientRequest(request) => sendClientRequest(request)
    case SendPrePrepareMessage      => sendConsensusMessage(BroadcastPrePrepare)
    case SendPrepareMessage         => sendConsensusMessage(BroadcastPrepare)
    case SendCommitMessage          => sendConsensusMessage(BroadcastCommit)

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

  private def sendClientRequest(request: ClientRequest)
                               (implicit config: ReplicaConfig, send: Send): Unit = {
    send.send(BroadcastRequest(request))
  }

  private def sendConsensusMessage(broadcast: BroadcastType)
                                  (implicit config: ReplicaConfig, send: Send): Unit = {
    send.send(broadcast)
  }

}
