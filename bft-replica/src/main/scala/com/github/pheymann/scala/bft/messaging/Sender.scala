package com.github.pheymann.scala.bft.messaging

import com.github.pheymann.scala.bft.messaging.SenderConnection.SenderConnectionState
import com.github.pheymann.scala.bft.replica.{ReplicaConfig, ReplicaContext}
import com.github.pheymann.scala.bft.util.AuthenticationGenerator
import com.github.pheymann.scala.bft.SessionKey

object Sender {

  final class SenderContext {

    private[Sender] val connections = collection.mutable.Map[Int, SenderConnectionState]()

  }

  private def broadcastConsensus(sender: SenderContext, messageGen: (Int, SenderConnectionState) => SignedConsensusMessage)
                                (implicit context: ReplicaContext): Unit = {
    import sender._

    connections.foreach { case (receiverId, state) =>
      state.socket.send(messageGen(receiverId, state))
    }
  }

  private def generateSignedMessage(generator: => ConsensusMessage, sessionKey: SessionKey)
                                   (implicit config: ReplicaConfig): SignedConsensusMessage = {
    val message = generator

    SignedConsensusMessage(
      message,
      AuthenticationGenerator.generateMAC(message, sessionKey)
    )
  }

  def broadcastPrePrepare(sender: SenderContext)
                         (implicit context: ReplicaContext): Unit = {
    import context.config

    broadcastConsensus(sender, (receiverId, state) => {
      generateSignedMessage(
        PrePrepareMessage(context.config.id, receiverId, context.view, context.sequenceNumber),
        state.sessionKey
      )
    })
  }

  def broadcastPrepare(sender: SenderContext)
                      (implicit context: ReplicaContext): Unit = {
    import context.config

    broadcastConsensus(sender, (receiverId, state) => {
      generateSignedMessage(
        PrepareMessage(context.config.id, receiverId, context.view, context.sequenceNumber),
        state.sessionKey
      )
    })
  }

  def broadcastCommit(sender: SenderContext)
                     (implicit context: ReplicaContext): Unit = {
    import context.config

    broadcastConsensus(sender, (receiverId, state) => {
      generateSignedMessage(
        CommitMessage(context.config.id, receiverId, context.view, context.sequenceNumber),
        state.sessionKey
      )
    })
  }

  def broadcastRequest(request: ClientRequest, sender: SenderContext)
                      (implicit context: ReplicaContext): Unit = {
    import context.config
    import sender._

    connections.foreach { case (receiverId, state) =>
      val delivery  = RequestDelivery(config.id, receiverId, context.view, context.sequenceNumber, request)

      state.socket.send(StartChunk(config.id, delivery.receiverId, context.sequenceNumber))

      RequestStream
        .generateChunks(delivery, config.chunkSize)
        .foreach { chunk =>
          val mac = AuthenticationGenerator.generateMAC(chunk, state.sessionKey)

          state.socket.send(SignedRequestChunk(config.id, delivery.receiverId, context.sequenceNumber, chunk, mac))
        }

      state.socket.send(EndChunk(config.id, delivery.receiverId, context.sequenceNumber))
    }
  }

}