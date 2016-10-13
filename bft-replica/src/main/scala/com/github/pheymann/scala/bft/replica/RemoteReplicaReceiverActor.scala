package com.github.pheymann.scala.bft.replica

import akka.pattern.ask
import akka.actor.{Actor, ActorRef}
import com.github.pheymann.scala.bft.Configuration
import com.github.pheymann.scala.bft.Types.SessionKey
import com.github.pheymann.scala.bft.replica.RemoteReplicaSenderActor.{SignedConsensusMessage, UnsignedMessage}
import com.github.pheymann.scala.bft.replica.messaging.MessageRouterActor
import com.github.pheymann.scala.bft.util.{ActorLoggingUtil, AuthenticationDigitsGenerator, AuthenticationDigitsVerification}

import scala.concurrent.Await
import scala.util.control.NonFatal

class RemoteReplicaReceiverActor(
                                  id: Long,

                                  host: String,
                                  port: Int,

                                  selfId: Long
                                )(
                                  routerRef:  ActorRef,
                                  config:     Configuration
                                ) extends Actor
                                  with    ActorLoggingUtil {

  import RemoteReplicaReceiverActor._
  import config.replicaConfig.keyRetrievalTimeout

  def this(data: ReplicaData, selfId: Long)
          (
            routerRef:  ActorRef,
            config:     Configuration
          ) = this(data.id, data.host, data.port, selfId)(routerRef, config)

  private val sessionKey   = try {
    val remoteSelect = context.actorSelection(s"akka.tcp://scala-bft-replica@$host:$port/user/${MessageRouterActor.name}")

    Await.result(remoteSelect ? RequestSessionKey, config.replicaConfig.keyRetrievalDuration).asInstanceOf[SessionKey]
  }
  catch {
    case NonFatal(cause) =>
      error(cause, s"fatal.no-session-key: {$selfId,$id}")
      //TODO stop application
      throw cause
  }

  override def receive = {
    case SignedConsensusMessage(message, mac) =>
      if (AuthenticationDigitsVerification.verifyMac(mac, AuthenticationDigitsGenerator.generateMAC(message, sessionKey))) {
        routerRef ! message
      }
      else
        error(s"message.invalid.signature: ${message.toLog}")

    case UnsignedMessage(message) => routerRef ! message

    case RequestSessionKey => sender() ! sessionKey
  }

}

object RemoteReplicaReceiverActor {

  case object RequestSessionKey

  def name(replicaId: Long): String = s"remote-replica-receiver-$replicaId"

}
