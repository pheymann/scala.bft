package com.github.pheymann.scala.bft.messaging

import akka.pattern.ask
import akka.actor.{Actor, ActorSelection}
import cats.data.Xor
import com.github.pheymann.scala.bft.SessionKey
import com.github.pheymann.scala.bft.replica.{ReplicaConfig, ReplicaEndpoint}
import com.github.pheymann.scala.bft.util.ActorLoggingUtil

import scala.concurrent.Await
import scala.util.control.NonFatal

class SenderActor(endpoint: ReplicaEndpoint, remoteSelect: ActorSelection) extends Actor with ActorLoggingUtil {

  infoLog(s"sender.start: ${endpoint.toLog}")

  override def receive = {
    case message => remoteSelect ! message
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

  def name(endpoint: ReplicaEndpoint): String = s"replica-sender_${endpoint.host}-${endpoint.port}"

}
