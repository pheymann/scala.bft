package com.github.pheymann.scala.bft.messaging

import com.github.pheymann.scala.bft.SessionKey
import cats.free.Free
import com.github.pheymann.scala.bft.replica.ServiceAction

sealed trait MessagingAction[R] extends ServiceAction[R]

object MessagingAction {

  final case class OpenSenderConnection(senderId: Int, sessionKey: SessionKey) extends MessagingAction[Unit]

  def openSenderConnection(senderId: Int, sessionKey: SessionKey): Free[ServiceAction, Unit] = {
    Free.liftF(OpenSenderConnection(senderId, sessionKey))
  }

  final case class CloseSenderConnection(senderId: Int) extends MessagingAction[Unit]

  def closeSenderConnection(senderId: Int): Free[ServiceAction, Unit] = {
    Free.liftF(CloseSenderConnection(senderId))
  }

  final case class SendConsensusMsgTo(receiverId: Int, message: SignedConsensusMessage) extends MessagingAction[Unit]

  def sendConsensusMsgTo(receiverId: Int, message: SignedConsensusMessage): Free[ServiceAction, Unit] = {
    Free.liftF(SendConsensusMsgTo(receiverId, message))
  }

  final case class SendStreamTo(receiverId: Int, stream: List[ChunkMessage]) extends MessagingAction[Unit]

  def sendStreamTo(receiverId: Int, stream: List[ChunkMessage]): Free[ServiceAction, Unit] = {
    Free.liftF(SendStreamTo(receiverId, stream))
  }

}
