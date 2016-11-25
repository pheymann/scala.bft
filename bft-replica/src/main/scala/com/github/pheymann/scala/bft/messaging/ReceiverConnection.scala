package com.github.pheymann.scala.bft.messaging

import cats.data.Xor
import com.github.pheymann.scala.bft.SessionKey
import com.github.pheymann.scala.bft.util.{ScalaBftLogger, SessionKeyGenerator}
import com.github.pheymann.scala.bft.messaging.Receiver.ReceiverContext
import com.github.pheymann.scala.bft.messaging.RequestStream.RequestStreamState
import org.slf4j.LoggerFactory

object ReceiverConnection {

  import ScalaBftLogger._

  case object NoMessage

  sealed trait ReceiverError

  case object ConnectionAlreadyOpenError  extends ReceiverError
  case object ConnectionNotOpenError      extends ReceiverError

  final case class ReceiverConnectionState(senderId: Int, sessionKey: SessionKey) {

    private[messaging] var streamStateOpt  = Option.empty[RequestStreamState]

    private[messaging] val messageBuffer   = collection.mutable.ListBuffer[ConsensusMessage]()

  }

  private implicit val log = LoggerFactory.getLogger("receiver.connection")

  def open(
            selfId:   Int,
            senderId: Int,
            sessionKey: SessionKey,
            context:    ReceiverContext
          ): Xor[ReceiverError, ReceiverConnectionState] = {
    import context._

    if (connections.contains(senderId)) {
      logWarn(s"exists: $senderId")
      Xor.left(ConnectionAlreadyOpenError)
    }
    else {
      val sessionKey  = SessionKeyGenerator.generateSessionKey(senderId, selfId)
      val state       = ReceiverConnectionState(senderId, sessionKey)

      logInfo(s"opened: $senderId")

      connections += senderId -> state

      Xor.right(ReceiverConnectionState(senderId, sessionKey))
    }
  }

  def close(senderId: Int, context: ReceiverContext): Xor[ReceiverError, Boolean] = {
    import context._

    if (connections.remove(senderId).isDefined) {
      logInfo(s"closed: $senderId")
      Xor.right(true)
    }
    else {
      logWarn(s"not.exists: $senderId")
      Xor.left(ConnectionNotOpenError)
    }
  }

}
