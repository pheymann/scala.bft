package com.github.pheymann.scala.bft.consensus

import cats.free.Free
import com.github.pheymann.scala.bft.messaging._
import com.github.pheymann.scala.bft.replica.ServiceAction

sealed trait ValidationAction[R] extends ServiceAction[R]

object ValidationAction {

  final case class ValidatePrePrepare(
                                       message:   PrePrepareMessage,
                                       delivery:  RequestDelivery,
                                       state:     ConsensusState
                                     ) extends ValidationAction[ConsensusState]

  def validate(
                message:   PrePrepareMessage,
                delivery:  RequestDelivery,
                state:     ConsensusState
              ): Free[ServiceAction, ConsensusState] = {
    Free.liftF(ValidatePrePrepare(message, delivery, state))
  }

  final case class ValidatePrepare(message: PrepareMessage, state: ConsensusState)  extends ValidationAction[ConsensusState]

  def validate(message: PrepareMessage, state: ConsensusState): Free[ServiceAction, ConsensusState] = {
    Free.liftF(ValidatePrepare(message, state))
  }

  final case class ValidateCommit(message: CommitMessage, state: ConsensusState)    extends ValidationAction[ConsensusState]

  def validate(message: CommitMessage, state: ConsensusState): Free[ServiceAction, ConsensusState] = {
    Free.liftF(ValidateCommit(message, state))
  }

}
