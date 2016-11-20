package com.github.pheymann.scala.bft.consensus

import cats.{Id, ~>}
import ValidationAction._
import com.github.pheymann.scala.bft.replica.ReplicaContext

class ValidationInterpreter()
                           (implicit context: ReplicaContext) extends (ValidationAction ~> Id) {

  import MessageValidation._

  override def apply[R](action: ValidationAction[R]): Id[R] = action match {
    case ValidatePrePrepare(message, delivery, state) => validatePrePrepare(message, delivery, state)
    case ValidatePrepare(message, state)              => validatePrepare(message, state)
    case ValidateCommit(message, state)               => validateCommit(message, state)
  }

}
