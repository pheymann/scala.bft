package com.github.pheymann.scala.bft.replica

import cats.{Id, ~>}
import com.github.pheymann.scala.bft.consensus.{SendCommitMessage, ValidationAction, ValidationProcessor}
import com.github.pheymann.scala.bft.storage.{StorageAction, StorageProcessor}
import com.github.pheymann.scala.bft.util.AuthenticationGeneratorT
import org.slf4j.Logger

class ReplicaProcessor(implicit config: ReplicaConfig, log: Logger) extends (ReplicaAction ~> Id) {

  override def apply[A](action: ReplicaAction[A]): Id[A] = action match {
    case SendCommitMessage(state)       => ??? //TODO implement send message
    case ExecuteRequest(state)          => ??? //TODO implement execute request

    case validate: ValidationAction[A]  => ValidationProcessor(validate)
    case store: StorageAction[A]        => StorageProcessor(store)
  }

}
