package com.github.pheymann.scala.bft.messaging

import akka.pattern.ask
import akka.actor.{Actor, ActorSelection}
import cats.data.Xor
import com.github.pheymann.scala.bft._
import com.github.pheymann.scala.bft.replica.{ReplicaConfig, ReplicaEndpoint}
import com.github.pheymann.scala.bft.util.ActorLoggingUtil

import scala.concurrent.Await
import scala.util.control.NonFatal

class SenderActor(endpoints: Seq[ReplicaEndpoint]) extends Actor
                                                   with    ActorLoggingUtil {

  import SenderActor._

  private val senderRefs: Map[Int, ActorSelection] = endpoints
    .map { endpoint =>
      val remoteRef = context.actorSelection(url(endpoint, ReceiverActor.name))

      endpoint.id -> remoteRef
    }(collection.breakOut)

  override def receive = {
    case msg: SignedConsensusMessage  => senderRefs.get(msg.message.receiverId).foreach(_ ! msg)
    case msg: SignedRequestChunk      => senderRefs.get(msg.receiverId).foreach(_ ! msg)
  }

}

object SenderActor {

  sealed trait SenderMessage

  case object RequestSessionKey extends SenderMessage

  case object KeyRequestTimeout

  def url(endpoint: ReplicaEndpoint, receiverName: String): String = {
    s"akka.tcp://scala-bft-replica@%s:%d/user/%s".format(
      endpoint.host,
      endpoint.port,
      receiverName
    )
  }

  def requestSessionKey(remoteSelect: ActorSelection)
                       (implicit config: ReplicaConfig): Xor[KeyRequestTimeout.type, SessionKey] = {
    import config.keyRequestTimeout

    try {
      Xor.right(Await
        .result(remoteSelect ? RequestSessionKey, config.keyRequestDuration)
        .asInstanceOf[SessionKey]
      )
    }
    catch {
      case NonFatal(cause) => Xor.left(KeyRequestTimeout)
    }
  }

  val name: String = "sender_router"

}
