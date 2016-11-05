package com.github.pheymann.scala.bft.messaging

import akka.actor.Actor
import com.github.pheymann.scala.bft.SessionKey
import com.github.pheymann.scala.bft.messaging.ReceiverConnectionHandler.ReceiverConnectionState
import com.github.pheymann.scala.bft.messaging.SenderActor.{CloseSenderConnection, OpenSenderConnection}
import com.github.pheymann.scala.bft.replica.ReplicaConfig
import com.github.pheymann.scala.bft.util.{ActorLoggingUtil, SessionKeyGenerator}

class ReceiverActor(implicit config: ReplicaConfig) extends Actor with ActorLoggingUtil {

  import ReceiverActor._

  logInfo("started")

  private val queue       = collection.mutable.Queue[Any]()
  private val connections = collection.mutable.Map[Int, ReceiverConnectionState]()

  override def receive = {
    case message: SignedConsensusMessage =>
      connections.get(message.message.senderId) match {
        case Some(state)  => ReceiverConnectionHandler.handleConsensus(message, state).foreach(msg => queue.enqueue(msg))
        case None         => logWarn(s"unknown.connection: ${message.message.senderId}")
      }

    case chunk: ChunkMessage =>
      connections.get(chunk.senderId) match {
        case Some(state)  => ReceiverConnectionHandler.handleStreams(chunk, state).foreach(_.foreach(msg => queue.enqueue(msg)))
        case None         => logWarn(s"unknown.connection: ${chunk.senderId}")
      }

    case Request =>
      if (queue.nonEmpty)
        sender() ! queue.dequeue()
      else
        sender() ! NoMessage

    case OpenConnection(senderId, sessionKey) =>
      if (connections.contains(senderId)) {
        logWarn(s"connection.exists: $senderId")
        sender() ! ConnectionAlreadyOpen
      }
      else {
        val sessionKey = SessionKeyGenerator.generateSessionKey(senderId, config.id)

        connections += senderId -> ReceiverConnectionState(senderId, sessionKey)

        logInfo(s"connection.opened: $senderId")

        config.senderRef  ! OpenSenderConnection(senderId, sender(), sessionKey)
        sender()          ! ConnectionSession(sessionKey)
      }

    case CloseConnection(senderId) =>
      if (connections.remove(senderId).isDefined) {
        logInfo(s"connection.closed: $senderId")
        config.senderRef ! CloseSenderConnection(senderId)
      }
      else
        logWarn(s"connection.not.exists: $senderId")
  }

}

object ReceiverActor {

  case object Request
  case object NoMessage

  final case class CloseConnection(senderId: Int)

  final case class OpenConnection(senderId: Int, sessionKey: SessionKey)
  final case class ConnectionSession(sessionKey: SessionKey)

  case object ConnectionAlreadyOpen

  val name = "receiver"

}
