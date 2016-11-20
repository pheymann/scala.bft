package com.github.pheymann.scala.bft.messaging

import akka.actor.{Actor, ActorRef}
import com.github.pheymann.scala.bft.replica.ReplicaEndpoint
import com.github.pheymann.scala.bft.util.ActorLoggingUtil

class SenderActor extends Actor with ActorLoggingUtil {

  import SenderActor._

  private val connections = collection.mutable.Map[Int, ActorRef]()

  override def receive = {
    case SendConsensusMsg(receiverId, message) => send(receiverId, _ ! message)

    case SendStream(receiverId, stream) => send(receiverId, receiverRef => {
      stream.foreach(receiverRef ! _)
    })

    case OpenSenderConnection(receiverId, receiverRef) =>
      if (connections.contains(receiverId))
        logWarn(s"exists: $receiverId")
      else
        connections += receiverId -> receiverRef

    case CloseSenderConnection(receiverId) =>
      if (connections.remove(receiverId).isDefined)
        logInfo(s"closed: $receiverId")
      else
        logWarn(s"not.exists: $receiverId")

  }

  def send(receiverId: Int, action: ActorRef => Unit): Unit = {
    connections.get(receiverId) match {
      case Some(receiverRef)  => action(receiverRef)
      case None               => logError(s"no-connection: $receiverId")
    }
  }

}

object SenderActor {

  sealed trait SenderOperations

  final case class SendConsensusMsg(receiverId: Int, message: ConsensusMessage) extends SenderOperations
  final case class SendStream(receiverId: Int, stream: List[ChunkMessage])      extends SenderOperations

  final case class OpenSenderConnection(receiverId: Int, receiverRef: ActorRef) extends SenderOperations
  final case class CloseSenderConnection(receiverId: Int)                       extends SenderOperations

  def url(endpoint: ReplicaEndpoint, receiverName: String): String = {
    s"akka.tcp://scala-bft-replica@%s:%d/user/%s".format(
      endpoint.host,
      endpoint.port,
      receiverName
    )
  }

  val name = "sender.io.connection"

}
