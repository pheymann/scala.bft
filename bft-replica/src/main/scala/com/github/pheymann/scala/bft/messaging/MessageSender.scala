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
    config.localSessionKeys.get(message.replicaId).fold[Xor[NoSessionKey, Mac]](
      Xor.left(NoSessionKey(message.replicaId))
    ) { sessionKey =>
      Xor.right(AuthenticationGenerator.generateMAC(message, sessionKey))
    }
  }

  def sendClientRequest(delivery: RequestDelivery, send: (Any, ReplicaConfig) => Unit = sendToActor)
                       (implicit config: ReplicaConfig): Xor[NoSessionKey, Boolean] = {
    config.localSessionKeys.get(delivery.replicaId).fold[Xor[NoSessionKey, Boolean]](
      Xor.left(NoSessionKey(delivery.replicaId))
    ) { sessionKey =>
      Xor.right {
        RequestStream
          .generateChunks(delivery, config.chunkSize)
          .foreach { chunk =>
            val mac = AuthenticationGenerator.generateMAC(chunk, sessionKey)

            send(SignedRequestChunk(chunk, mac), config)
          }

        true
      }
    }
  }

  private def sendToActor(msg: Any, config: ReplicaConfig): Unit = {
    config.senderRef ! msg
  }

}
