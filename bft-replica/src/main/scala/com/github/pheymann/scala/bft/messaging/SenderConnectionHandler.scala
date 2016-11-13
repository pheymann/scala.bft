package com.github.pheymann.scala.bft.messaging

import com.github.pheymann.scala.bft.SessionKey
import com.github.pheymann.scala.bft.replica.ReplicaContext

object SenderConnectionHandler {

  final case class SenderConnectionState(sessionKey: SessionKey)

  def prePrepare(receiverId: Int)
                        (implicit context: ReplicaContext): PrePrepareMessage = {
    PrePrepareMessage(context.config.id, receiverId, context.view, context.sequenceNumber)
  }

  def prepare(receiverId: Int)
             (implicit context: ReplicaContext): PrepareMessage = {
    PrepareMessage(context.config.id, receiverId, context.view, context.sequenceNumber)
  }

  def commit(receiverId: Int)
            (implicit context: ReplicaContext): CommitMessage = {
    CommitMessage(context.config.id, receiverId, context.view, context.sequenceNumber)
  }

}
