package com.github.pheymann.scala.bft.messaging

import akka.actor.{Actor, ActorRef}
import com.github.pheymann.scala.bft._
import com.github.pheymann.scala.bft.messaging.SenderConnectionHandler.SenderConnectionState
import com.github.pheymann.scala.bft.replica.{ReplicaConfig, ReplicaEndpoint}
import com.github.pheymann.scala.bft.util.{ActorLoggingUtil, AuthenticationGenerator}

class SenderActor(implicit config: ReplicaConfig) extends Actor with ActorLoggingUtil {

  import SenderActor._

  private val connections = collection.mutable.Map[Int, (SenderConnectionState, ActorRef)]()

  override def receive = {
    case BroadcastPrePrepare =>
      connections.foreach { case (receiverId, (state, receiverRef)) =>
        val message = SenderConnectionHandler.prePrepare(receiverId)

        sendMessage(receiverRef, message, state)
      }

    case BroadcastPrepare =>
      connections.foreach { case (receiverId, (state, receiverRef)) =>
        val message = SenderConnectionHandler.prepare(receiverId)

        sendMessage(receiverRef, message, state)
      }

    case BroadcastCommit =>
      connections.foreach { case (receiverId, (state, receiverRef)) =>
        val message = SenderConnectionHandler.commit(receiverId)

        sendMessage(receiverRef, message, state)
      }

    case BroadcastRequest(request) =>
      connections.foreach { case (receiverId, (state, receiverRef)) =>
        val delivery = RequestDelivery(config.id, receiverId, config.view, config.sequenceNumber, request)

        receiverRef ! StartChunk(config.id, delivery.receiverId, config.sequenceNumber)

        RequestStream
          .generateChunks(delivery, config.chunkSize)
          .foreach { chunk =>
            val mac = AuthenticationGenerator.generateMAC(chunk, state.sessionKey)

            receiverRef ! SignedRequestChunk(config.id, delivery.receiverId, config.sequenceNumber, chunk, mac)
          }

        receiverRef ! EndChunk(config.id, delivery.receiverId, config.sequenceNumber)
      }

    case OpenSenderConnection(receiverId, receiverRef, sessionKey) =>
      if (connections.contains(receiverId))
        logWarn(s"connection.exists: $receiverId")
      else
        connections += receiverId -> (SenderConnectionState(sessionKey), receiverRef)

    case CloseSenderConnection(receiverId) =>
      if (connections.remove(receiverId).isDefined)
        logInfo(s"connection.closed: $receiverId")
      else
        logWarn(s"connection.not.exists: $receiverId")

  }

  private def sendMessage(
                           receiverRef: ActorRef,
                           message:     ConsensusMessage,
                           state:       SenderConnectionState
                         ): Unit = {
    val signedMessage = SignedConsensusMessage(
      message,
      AuthenticationGenerator.generateMAC(message, state.sessionKey)
    )

    receiverRef ! signedMessage
  }

//  private def requestSessionKey(receiverRef: ActorRef)
//                               (implicit config: ReplicaConfig): Xor[Throwable, SessionKey] = {
//    import config.keyRequestTimeout
//
//    try {
//      Xor.right(
//        Await
//          .result(receiverRef ? OpenConnection(config.id), config.keyRequestDuration)
//          .asInstanceOf[SessionKey]
//      )
//    }
//    catch {
//      case NonFatal(cause) => Xor.left(cause)
//    }
//  }

}

object SenderActor {

  sealed trait BroadcastType

  case object BroadcastPrePrepare extends BroadcastType
  case object BroadcastPrepare    extends BroadcastType
  case object BroadcastCommit     extends BroadcastType

  final case class BroadcastRequest(request: ClientRequest) extends BroadcastType

  final case class OpenSenderConnection(receiverId: Int, receiverRef: ActorRef, sessionKey: SessionKey)
  final case class CloseSenderConnection(receiverId: Int)

  def url(endpoint: ReplicaEndpoint, receiverName: String): String = {
    s"akka.tcp://scala-bft-replica@%s:%d/user/%s".format(
      endpoint.host,
      endpoint.port,
      receiverName
    )
  }

  val name = "sender"

}
