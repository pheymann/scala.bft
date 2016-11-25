package com.github.pheymann.scala.bft.messaging

import cats.free.Free
import com.github.pheymann.scala.bft.replica.ServiceAction

sealed trait MessagingAction[R] extends ServiceAction[R]

object MessagingAction {

  final case class BroadcastRequest(request: ClientRequest) extends MessagingAction[Unit]

  def broadcastRequest(request: ClientRequest): Free[ServiceAction, Unit] = {
    Free.liftF(BroadcastRequest(request))
  }

  case object BroadcastPrePrepare extends MessagingAction[Unit]

  def broadcastPrePrepare(): Free[ServiceAction, Unit] = {
    Free.liftF(BroadcastPrePrepare)
  }

  case object BroadcastPrepare extends MessagingAction[Unit]

  def broadcastPrepare(): Free[ServiceAction, Unit] = {
    Free.liftF(BroadcastPrepare)
  }

  case object BroadcastCommit extends MessagingAction[Unit]

  def broadcastCommit(): Free[ServiceAction, Unit] = {
    Free.liftF(BroadcastCommit)
  }

}
