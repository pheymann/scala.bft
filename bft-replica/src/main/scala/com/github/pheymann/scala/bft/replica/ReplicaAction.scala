package com.github.pheymann.scala.bft.replica

import cats.free.Free
import com.github.pheymann.scala.bft.consensus.ConsensusState

trait ReplicaAction[A]

case object RequestMessage extends ReplicaAction[Any]

final case class ExecuteRequest(state: ConsensusState) extends ReplicaAction[ConsensusState]

object ReplicaLifting {

  final case class Assign[A](value: A) extends ReplicaAction[A]

  def process[A](action: ReplicaAction[A]): Free[ReplicaAction, A]  = Free.liftF(action)
  def assign[A](value: A): Free[ReplicaAction, A]                   = Free.liftF(Assign(value))

}
