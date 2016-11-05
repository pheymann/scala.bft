package com.github.pheymann.scala.bft.messaging

import akka.actor.Actor
import com.github.pheymann.scala.bft.SessionKey
import com.github.pheymann.scala.bft.messaging.ConnectionHandler.ConnectionState
import com.github.pheymann.scala.bft.replica.ReplicaConfig
import com.github.pheymann.scala.bft.util.ActorLoggingUtil

class ReceiverActor(implicit config: ReplicaConfig) extends Actor with ActorLoggingUtil {

  import ReceiverActor._

  logInfo("started")

  private val queue       = collection.mutable.Queue[Any]()
  private val connections = collection.mutable.Map[Int, ConnectionState]()

  override def receive = {
    case message: SignedConsensusMessage =>
      connections.get(message.message.senderId) match {
        case Some(state)  => ConnectionHandler.handleMessage(message, state).foreach(_.foreach(msg => queue.enqueue(msg)))
        case None         => logWarn(s"unknown.connection: ${message.message.senderId}")
      }

    case chunk: SignedChunkMessage =>
      connections.get(chunk.senderId) match {
        case Some(state)  => ConnectionHandler.handleMessage(chunk, state).foreach(_.foreach(msg => queue.enqueue(msg)))
        case None         => logWarn(s"unknown.connection: ${chunk.senderId}")
      }

    case Request =>
      if (queue.nonEmpty)
        sender() ! queue.dequeue()
      else
        sender() ! NoMessage

    case OpenConnection(senderId, sessionKey) =>
      if (connections.contains(senderId))
        logWarn(s"connection.exists: $senderId")
      else
        connections += senderId -> ConnectionState(senderId, sessionKey)
  }

}

object ReceiverActor {

  case object Request
  case object NoMessage

  final case class OpenConnection(senderId: Int, sessionKey: SessionKey)

  val name = "receiver"

}
