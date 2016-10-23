package com.github.pheymann.scala.bft.replica

import cats.free.Free
import com.github.pheymann.scala.bft.consensus.ConsensusState

trait ReplicaAction[A]

final case class ExecuteRequest(state: ConsensusState) extends ReplicaAction[ConsensusState]

object ReplicaLifting {

  def process[A](action: ReplicaAction[A]): Free[ReplicaAction, A] = Free.liftF(action)

}
