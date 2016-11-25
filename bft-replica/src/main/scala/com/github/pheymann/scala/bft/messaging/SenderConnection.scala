package com.github.pheymann.scala.bft.messaging

import com.github.pheymann.scala.bft.SessionKey
import com.github.pheymann.scala.bft.util.ScalaBftLogger._
import org.slf4j.LoggerFactory

object SenderConnection {

  trait SenderSocket {

    def send(msg: Any): Unit

  }

  final case class SenderConnectionState(sessionKey: SessionKey, socket: SenderSocket)

  private implicit val log = LoggerFactory.getLogger("sender.connection")

  def open(
            senderId:     Int,
            sessionKey:   SessionKey,
            socket:       SenderSocket,
            connections:  collection.mutable.Map[Int, SenderConnectionState]
          ): collection.mutable.Map[Int, SenderConnectionState] = {
    if (connections.contains(senderId))
      logWarn(s"exists: $senderId")
    else
      connections += senderId -> SenderConnectionState(sessionKey, socket)
    connections
  }

  def close(
             senderId:    Int,
             connections: collection.mutable.Map[Int, SenderConnectionState]
           ): collection.mutable.Map[Int, SenderConnectionState] = {
    if (connections.remove(senderId).isDefined)
      logInfo(s"closed: $senderId")
    else
      logWarn(s"not.exists: $senderId")
    connections
  }

}
