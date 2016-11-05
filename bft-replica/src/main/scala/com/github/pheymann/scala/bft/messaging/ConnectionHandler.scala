package com.github.pheymann.scala.bft.messaging

import com.github.pheymann.scala.bft.SessionKey
import com.github.pheymann.scala.bft.messaging.RequestStream.RequestStreamState
import com.github.pheymann.scala.bft.replica.ReplicaConfig
import com.github.pheymann.scala.bft.util.AuthenticationVerification
import org.slf4j.LoggerFactory

object ConnectionHandler {

  import com.github.pheymann.scala.bft.util.ScalaBftLogger._

  private implicit val log = LoggerFactory.getLogger("connection.handler")

  final case class ConnectionState(senderId: Int, sessionKey: SessionKey) {

    private[ConnectionHandler] var streamStateOpt  = Option.empty[RequestStreamState]

    private[ConnectionHandler] val messageBuffer   = collection.mutable.ListBuffer[SignedConsensusMessage]()

  }

  def handleMessage(message: Any, state: ConnectionState)
                   (implicit config: ReplicaConfig): Option[Seq[Any]] = message match {
    case message: SignedConsensusMessage =>
      if (verify(message, state.sessionKey))
        state.streamStateOpt.fold[Option[Seq[Any]]] {
          Some(Seq(message))
        } { _ =>
          state.messageBuffer += message
          None
        }
      else {
        logWarn(s"invalid.message: ${message.message.toLog}")
        None
      }

    case start: StartChunk =>
      state.streamStateOpt.fold {
        if (verify(start, state))
          state.streamStateOpt = Some(RequestStreamState(start.sequenceNumber))
        else
          logWarn(s"invalid.start.chunk: $start")
      } { _ =>
        logWarn(s"unexpected.start.chunk: $start")
      }

      None

    case end: EndChunk =>
      state.streamStateOpt.fold {
        logWarn(s"unexpected.end.chunk: $end")
        Option.empty[Seq[Any]]
      } { streamState =>
        if (verify(end, state)) {
          //TODO handle exceptions
          val request = RequestStream.generateRequest(state.streamStateOpt.get)
          val messages = Seq(request) ++ state.messageBuffer

          state.streamStateOpt = None
          state.messageBuffer.clear()
          Some(messages)
        }
        else {
          logWarn(s"invalid.end.chunk: $end")
          None
        }
      }


    case chunk: SignedRequestChunk =>
      state.streamStateOpt.fold {
        logWarn(s"unexpected.chunk: $chunk")
      } { streamState =>
        if (verify(chunk, state.sessionKey))
          RequestStream.collectChunks(chunk.chunk, streamState)
        else
          logWarn(s"invalid.chunk: $chunk")
      }

      None
  }

  private def verify(message: SignedConsensusMessage, sessionKey: SessionKey)
                    (implicit config: ReplicaConfig): Boolean = {
    AuthenticationVerification.verify(message, sessionKey)
  }

  private def verify(chunk: SignedRequestChunk, sessionKey: SessionKey)
                    (implicit config: ReplicaConfig): Boolean = {
    AuthenticationVerification.verify(chunk, sessionKey)
  }

  private def verify(signalChunk: ChunkMessage, state: ConnectionState): Boolean = {
    signalChunk.senderId == state.senderId &&
      signalChunk.sequenceNumber == state.streamStateOpt.get.sequenceNumber
  }

}
