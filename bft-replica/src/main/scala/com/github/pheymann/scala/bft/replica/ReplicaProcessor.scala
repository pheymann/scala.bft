package com.github.pheymann.scala.bft.replica

import cats.{Id, ~>}
import com.github.pheymann.scala.bft.consensus._
import com.github.pheymann.scala.bft.replica.ReplicaLifting.Assign
import com.github.pheymann.scala.bft.storage._
import org.slf4j.Logger

class ReplicaProcessor(implicit config: ReplicaConfig, log: Logger) extends (ReplicaAction ~> Id) {

  override def apply[A](action: ReplicaAction[A]): Id[A] = action match {
    case ValidatePrePrepare(message, delivery, state) => MessageValidation.validatePrePrepare(message, delivery, state)
    case ValidatePrepare(message, state)  => MessageValidation.validatePrepare(message, state)
    case ValidateCommit(message, state)   => MessageValidation.validateCommit(message, state)

    case SendPrePrepareMessage(state)   => ??? //TODO implement send message
    case SendPrepareMessage(state)      => ??? //TODO implement send message
    case SendCommitMessage(state)       => ??? //TODO implement send message

    case StorePrePrepare(request, message) => ??? //TODO implemented storage
    case StorePrepare(message) => ??? //TODO implemented storage
    case StoreCommit(message)  => ??? //TODO implemented storage

    case ExecuteRequest(state)          => ??? //TODO implement execute request

    case Assign(value) => value
  }

}
