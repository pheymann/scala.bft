package com.github.pheymann.scala.bft.messaging

import com.github.pheymann.scala.bft.SessionKey
import com.github.pheymann.scala.bft.messaging.Sender.SenderContext
import com.github.pheymann.scala.bft.util.ScalaBftLogger._
import org.slf4j.LoggerFactory

object SenderConnection {

  type SendThroughSocket = ScalaBftMessage => Unit

  final case class SenderConnectionState(sessionKey: SessionKey, socket: SendThroughSocket)

  private implicit val log = LoggerFactory.getLogger("sender.connection")

  def open(
            senderId:     Int,
            sessionKey:   SessionKey,
            socket:       SendThroughSocket,
            context:      SenderContext
          ): SenderContext = {
    import context._

    if (connections.contains(senderId))
      logWarn(s"exists: $senderId")
    else
      connections += senderId -> SenderConnectionState(sessionKey, socket)
    context
  }

  def close(
             senderId:  Int,
             context:   SenderContext
           ): SenderContext = {
    import context._

    if (connections.remove(senderId).isDefined)
      logInfo(s"closed: $senderId")
    else
      logWarn(s"not.exists: $senderId")
    context
  }

}
