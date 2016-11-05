package com.github.pheymann.scala.bft.messaging

import com.github.pheymann.scala.bft.SessionKey
import com.github.pheymann.scala.bft.replica.ReplicaConfig

object SenderConnectionHandler {

  final case class SenderConnectionState(sessionKey: SessionKey)

  def prePrepare(receiverId: Int)
                        (implicit config: ReplicaConfig): PrePrepareMessage = {
    PrePrepareMessage(config.id, receiverId, config.view, config.sequenceNumber)
  }

  def prepare(receiverId: Int)
             (implicit config: ReplicaConfig): PrepareMessage = {
    PrepareMessage(config.id, receiverId, config.view, config.sequenceNumber)
  }

  def commit(receiverId: Int)
            (implicit config: ReplicaConfig): CommitMessage = {
    CommitMessage(config.id, receiverId, config.view, config.sequenceNumber)
  }

}
