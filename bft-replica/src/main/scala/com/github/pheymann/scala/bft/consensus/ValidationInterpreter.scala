package com.github.pheymann.scala.bft.consensus

import cats.{Id, ~>}
import ValidationAction._
import com.github.pheymann.scala.bft.replica.ReplicaContext

object ValidationInterpreter {

  def validation()
                (implicit context: ReplicaContext): ValidationAction ~> Id =  new (ValidationAction ~> Id) {

    import MessageValidation._

    override def apply[R](action: ValidationAction[R]): Id[R] = action match {
      case ValidatePrePrepare(message, delivery, state) => validatePrePrepare(message, delivery, state)
      case ValidatePrepare(message, state)              => validatePrepare(message, state)
      case ValidateCommit(message, state)               => validateCommit(message, state)
    }

  }

}
