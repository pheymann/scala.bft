package com.github.pheymann.scala.bft.consensus

import cats.Id
import com.github.pheymann.scala.bft.replica.ReplicaConfig
import org.slf4j.Logger

object ValidationProcessor {

  def apply[A](action: ValidationAction[A])
              (implicit config: ReplicaConfig, log: Logger): Id[A] = action match {
    case ValidatePrepare(message, state)  => MessageValidation.validatePrepare(message, state)
    case ValidateCommit(message, state)   => MessageValidation.validateCommit(message, state)
  }

}
