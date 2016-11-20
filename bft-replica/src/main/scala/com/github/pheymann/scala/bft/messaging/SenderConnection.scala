package com.github.pheymann.scala.bft.messaging

import com.github.pheymann.scala.bft.SessionKey
import com.github.pheymann.scala.bft.replica.ReplicaContext
import com.github.pheymann.scala.bft.util.ScalaBftLogger._
import org.slf4j.LoggerFactory

object SenderConnection {

  final case class SenderConnectionState(sessionKey: SessionKey)

  private implicit val log = LoggerFactory.getLogger("sender.connection")

  def open(
            senderId:     Int,
            sessionKey:   SessionKey,
            connections:  collection.mutable.Map[Int, SenderConnectionState]
          ): collection.mutable.Map[Int, SenderConnectionState] = {
    if (connections.contains(senderId))
      logWarn(s"exists: $senderId")
    else
      connections += senderId -> SenderConnectionState(sessionKey)
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
