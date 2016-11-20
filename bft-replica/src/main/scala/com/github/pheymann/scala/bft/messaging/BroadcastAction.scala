package com.github.pheymann.scala.bft.messaging

import cats.free.Free
import com.github.pheymann.scala.bft.consensus.ConsensusState
import com.github.pheymann.scala.bft.replica.ServiceAction

sealed trait BroadcastAction[R] extends ServiceAction[R]

object BroadcastAction {

  final case class BroadcastRequest(request: ClientRequest, state: ConsensusState) extends BroadcastAction[Unit]

  def broadcastRequest(request: ClientRequest, state: ConsensusState): Free[ServiceAction, Unit] = {
    Free.liftF(BroadcastRequest(request, state))
  }

  final case class BroadcastPrePrepare(state: ConsensusState) extends BroadcastAction[Unit]

  def broadcastPrePrepare(state: ConsensusState): Free[ServiceAction, Unit] = {
    Free.liftF(BroadcastPrePrepare(state))
  }

  final case class BroadcastPrepare(state: ConsensusState) extends BroadcastAction[Unit]

  def broadcastPrepare(state: ConsensusState): Free[ServiceAction, Unit] = {
    Free.liftF(BroadcastPrepare(state))
  }

  final case class BroadcastCommit(state: ConsensusState) extends BroadcastAction[Unit]

  def broadcastCommit(state: ConsensusState): Free[ServiceAction, Unit] = {
    Free.liftF(BroadcastCommit(state))
  }

}
