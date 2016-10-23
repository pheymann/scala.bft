package com.github.pheymann.scala.bft.consensus

import cats.free.Free
import com.github.pheymann.scala.bft.messaging.ConsensusMessage
import com.github.pheymann.scala.bft.replica.ReplicaAction


sealed trait ValidationAction[A] extends ReplicaAction[A]

final case class ValidatePrepare(message: ConsensusMessage, state: ConsensusState)  extends ValidationAction[ConsensusState]
final case class ValidateCommit(message: ConsensusMessage, state: ConsensusState)   extends ValidationAction[ConsensusState]

object ValidationLifting {

  def validate[A](action: ValidationAction[A]): Free[ReplicaAction, A] = Free.liftF(action)

}
