package com.github.pheymann.scala.bft.messaging

import com.github.pheymann.scala.bft.SessionKey
import com.github.pheymann.scala.bft.messaging.ReceiverConnection.ReceiverConnectionState
import com.github.pheymann.scala.bft.messaging.RequestStream.RequestStreamState
import com.github.pheymann.scala.bft.replica.{ReplicaConfig, ReplicaContext}
import com.github.pheymann.scala.bft.util.AuthenticationVerification
import org.slf4j.LoggerFactory

object Receiver {

  import com.github.pheymann.scala.bft.util.ScalaBftLogger._

  case object NoMessage

  final class ReceiverContext {

    private[messaging] val queue       = collection.mutable.Queue[Any]()
    private[messaging] val connections = collection.mutable.Map[Int, ReceiverConnectionState]()

  }

  private implicit val log = LoggerFactory.getLogger("receiver.connections")

  def addConsensusMessage(message: SignedConsensusMessage, receiver: ReceiverContext)
                         (implicit context: ReplicaContext): Unit = {
    import context.config
    import receiver._

    connections.get(message.message.senderId) match {
      case Some(state)  => handleConsensus(message, state).foreach(msg => queue.enqueue(msg))
      case None         => logWarn(s"unknown.connection: ${message.message.senderId}")
    }
  }

  private def handleConsensus(signedMessage: SignedConsensusMessage, state: ReceiverConnectionState)
                             (implicit config: ReplicaConfig): Option[ConsensusMessage] = {
    if (verify(signedMessage, state.sessionKey))
      state.streamStateOpt.fold[Option[ConsensusMessage]] {
        Some(signedMessage.message)
      } { _ =>
        state.messageBuffer += signedMessage.message
        None
      }
    else {
      logWarn(s"${signedMessage.message.senderId}.invalid.message: ${signedMessage.message.toLog}")
      None
    }
  }

  def addChunkMessage(chunk: ChunkMessage, receiver: ReceiverContext)
                     (implicit context: ReplicaContext): Unit = {
    import context.config
    import receiver._

    connections.get(chunk.senderId) match {
      case Some(state)  => handleStreams(chunk, state).foreach(_.foreach(msg => queue.enqueue(msg)))
      case None         => logWarn(s"unknown.connection: ${chunk.senderId}")
    }
  }

  private def handleStreams(chunk: ChunkMessage, state: ReceiverConnectionState)
                           (implicit config: ReplicaConfig): Option[Seq[Any]] = chunk match {
    case start: StartChunk =>
      state.streamStateOpt.fold {
        state.streamStateOpt = Some(RequestStreamState(start.sequenceNumber))
      } { _ =>
        logWarn(s"${chunk.senderId}.unexpected.start.chunk: $start")
      }

      None

    case end: EndChunk =>
      state.streamStateOpt.fold {
        logWarn(s"${chunk.senderId}.unexpected.end.chunk: $end")
        Option.empty[Seq[Any]]
      } { streamState =>
        if (verify(end, state)) {
          def collectMessages(requestResult: Seq[Any] = Nil): Some[Seq[Any]] = {
            val messages = requestResult ++ state.messageBuffer

            state.streamStateOpt = None
            state.messageBuffer.clear()
            Some(messages)
          }

          RequestStream.generateRequest(state.streamStateOpt.get).fold[Option[Seq[Any]]](
            { cause =>
              logError(cause, s"${chunk.senderId}.invalid.stream")
              collectMessages()
            }, { request =>
              collectMessages(Seq(request))
            }
          )
        }
        else {
          logWarn(s"${chunk.senderId}.invalid.end.chunk: $end")
          None
        }
      }


    case chunk: SignedRequestChunk =>
      state.streamStateOpt.fold {
        logWarn(s"${chunk.senderId}.unexpected.chunk: $chunk")
      } { streamState =>
        if (verify(chunk, state.sessionKey))
          RequestStream.collectChunks(chunk.chunk, streamState)
        else
          logWarn(s"${chunk.senderId}.invalid.chunk: $chunk")
      }

      None
  }

  def retrieveMessage(receiver: ReceiverContext): Any = {
    import receiver.queue

    if (queue.nonEmpty)
      queue.dequeue()
    else
      NoMessage
  }

  private def verify(message: SignedConsensusMessage, sessionKey: SessionKey)
                    (implicit config: ReplicaConfig): Boolean = {
    AuthenticationVerification.verify(message, sessionKey)
  }

  private def verify(chunk: SignedRequestChunk, sessionKey: SessionKey)
                    (implicit config: ReplicaConfig): Boolean = {
    AuthenticationVerification.verify(chunk, sessionKey)
  }

  private def verify(signalChunk: ChunkMessage, state: ReceiverConnectionState): Boolean = {
    signalChunk.senderId == state.senderId &&
      signalChunk.sequenceNumber == state.streamStateOpt.get.sequenceNumber
  }

}
