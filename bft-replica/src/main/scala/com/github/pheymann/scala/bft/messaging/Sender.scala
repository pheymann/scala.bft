package com.github.pheymann.scala.bft.messaging

import cats.free.Free
import com.github.pheymann.scala.bft.messaging.SenderConnection.SenderConnectionState
import com.github.pheymann.scala.bft.replica.{ReplicaConfig, ReplicaContext, ServiceAction}
import com.github.pheymann.scala.bft.util.AuthenticationGenerator
import MessagingAction._
import com.github.pheymann.scala.bft.SessionKey

object Sender {

  import cats.implicits._

  final class SenderContext {

    private[Sender] val connections = collection.mutable.Map[Int, SenderConnectionState]()

  }

  def broadcastPrePrepare(sender: SenderContext)
                         (implicit context: ReplicaContext): Free[ServiceAction, List[Unit]] = {
    import sender._
    import context.config

    val receiverMessages: List[Free[ServiceAction, Unit]] = connections
      .map { case (receiverId, state) =>
        val signedMessage = generateSignedMessage(
          PrePrepareMessage(context.config.id, receiverId, context.view, context.sequenceNumber),
          state.sessionKey
        )

        sendConsensusMsgTo(receiverId, signedMessage)
      }(collection.breakOut)

    Free.catsFreeMonadForFree.sequence(receiverMessages)
  }

  def broadcastPrepare(sender: SenderContext)
                      (implicit context: ReplicaContext): Free[ServiceAction, List[Unit]] = {
    import sender._
    import context.config

    val receiverMessages: List[Free[ServiceAction, Unit]] = connections
      .map { case (receiverId, state) =>
        val signedMessage = generateSignedMessage(
          PrepareMessage(context.config.id, receiverId, context.view, context.sequenceNumber),
          state.sessionKey
        )

        sendConsensusMsgTo(receiverId, signedMessage)
      }(collection.breakOut)

    Free.catsFreeMonadForFree.sequence(receiverMessages)
  }

  def broadcastCommit(sender: SenderContext)
                     (implicit context: ReplicaContext): Free[ServiceAction, List[Unit]] = {
    import sender._
    import context.config

    val receiverMessages: List[Free[ServiceAction, Unit]] = connections
      .map { case (receiverId, state) =>
        val signedMessage = generateSignedMessage(
          CommitMessage(context.config.id, receiverId, context.view, context.sequenceNumber),
          state.sessionKey
        )

        sendConsensusMsgTo(receiverId, signedMessage)
      }(collection.breakOut)

    Free.catsFreeMonadForFree.sequence(receiverMessages)
  }

  private def generateSignedMessage(generator: => ConsensusMessage, sessionKey: SessionKey)
                                   (implicit config: ReplicaConfig): SignedConsensusMessage = {
    val message = generator

    SignedConsensusMessage(
      message,
      AuthenticationGenerator.generateMAC(message, sessionKey)
    )
  }

  def broadcastRequest(request: ClientRequest, sender: SenderContext)
                      (implicit context: ReplicaContext): Free[ServiceAction, List[Unit]] = {
    import context.config
    import sender._

    val receiverStreams: List[Free[ServiceAction, Unit]] = connections
      .map { case (receiverId, state) =>
        val delivery  = RequestDelivery(config.id, receiverId, context.view, context.sequenceNumber, request)
        val stream    = collection.mutable.ListBuffer[ChunkMessage]()

        stream += StartChunk(config.id, delivery.receiverId, context.sequenceNumber)

        stream ++= RequestStream
          .generateChunks(delivery, config.chunkSize)
          .map { chunk =>
            val mac = AuthenticationGenerator.generateMAC(chunk, state.sessionKey)

            SignedRequestChunk(config.id, delivery.receiverId, context.sequenceNumber, chunk, mac)
          }

        stream += EndChunk(config.id, delivery.receiverId, context.sequenceNumber)

        sendStreamTo(receiverId, stream.result())
      }(collection.breakOut)

    Free.catsFreeMonadForFree.sequence(receiverStreams)
  }

}
