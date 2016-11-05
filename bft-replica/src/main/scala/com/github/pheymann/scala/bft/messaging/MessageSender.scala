package com.github.pheymann.scala.bft.messaging

import cats.data.Xor
import com.github.pheymann.scala.bft.Mac
import com.github.pheymann.scala.bft.replica.ReplicaConfig
import com.github.pheymann.scala.bft.util.AuthenticationGenerator

object MessageSender {

  sealed trait SenderError

  final case class NoSessionKey(replicaId: Int) extends SenderError

  def sendConsensusMessage(message: ConsensusMessage, send: (Any, ReplicaConfig) => Unit = sendToActor)
                          (implicit config: ReplicaConfig): Xor[SenderError, Boolean] = {
    signConsensusMessage(message).map { mac =>
      send(SignedConsensusMessage(message, mac), config)
      true
    }
  }

  private def signConsensusMessage(message: ConsensusMessage)
                                  (implicit config: ReplicaConfig): Xor[NoSessionKey, Mac] = {
    config.senderSessions.get(message.receiverId).fold[Xor[NoSessionKey, Mac]](
      Xor.left(NoSessionKey(message.receiverId))
    ) { sessionKey =>
      Xor.right(AuthenticationGenerator.generateMAC(message, sessionKey))
    }
  }

  def sendClientRequest(delivery: RequestDelivery, send: (Any, ReplicaConfig) => Unit = sendToActor)
                       (implicit config: ReplicaConfig): Xor[NoSessionKey, Boolean] = {
    config.senderSessions.get(delivery.receiverId).fold[Xor[NoSessionKey, Boolean]](
      Xor.left(NoSessionKey(delivery.receiverId))
    ) { sessionKey =>
      Xor.right {
        RequestStream
          .generateChunks(delivery, config.chunkSize)
          .foreach { chunk =>
            val mac = AuthenticationGenerator.generateMAC(chunk, sessionKey)

            send(SignedRequestChunk(config.id, delivery.receiverId, config.sequenceNumber, chunk, mac), config)
          }

        true
      }
    }
  }

  private def sendToActor(msg: Any, config: ReplicaConfig): Unit = {
    config.senderRef ! msg
  }

}
