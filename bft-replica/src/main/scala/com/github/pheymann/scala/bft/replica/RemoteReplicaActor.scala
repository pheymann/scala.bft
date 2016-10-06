package com.github.pheymann.scala.bft.replica

import akka.actor.Actor
import akka.pattern.ask
import com.github.pheymann.scala.bft.BftReplicaConfig
import com.github.pheymann.scala.bft.Types.{Mac, SessionKey}
import com.github.pheymann.scala.bft.consensus.ConsensusMessage
import com.github.pheymann.scala.bft.model.{DataChunk, StartChunkStream}
import com.github.pheymann.scala.bft.util.{ActorLoggingUtil, AuthenticationDigitsGenerator, AuthenticationDigitsVerification}

import scala.concurrent.Await
import scala.util.control.NonFatal

case class RemoteReplicaActor(
                                id: Long,

                                host: String,
                                port: Int,

                                selfId: Long
                             )  extends Actor
                                with    ActorLoggingUtil {

  import RemoteReplicaActor._
  import BftReplicaConfig.keyRetrievalTimeout

  def this(data: ReplicaData, selfId: Long) = this(data.id, data.host, data.port, selfId)

  private val remoteSelect = context.actorSelection(s"akka.tcp://scala-bft-replica@$host:$port/user/${BftReplicaConfig.messageRouterName}")

  private val sessionKey   = try {
    Await.result(remoteSelect ? RequestSessionKey(selfId), BftReplicaConfig.keyRetrievalDuration).asInstanceOf[SessionKey]
  }
  catch {
    case NonFatal(cause) =>
      error(cause, s"fatal.no-session-key: {$selfId,$id}")
      throw cause
  }

  override def receive = {
    // received consensus message
    case SignedConsensusMessage(message, mac) =>
      if (AuthenticationDigitsVerification.verifyMac(mac, AuthenticationDigitsGenerator.generateMAC(message, sessionKey))) {
        sender() ! message
      }
      else
        error(s"message.invalid.signature: ${message.toLog}")

    // send consensus message with signature
    case message: ConsensusMessage =>
      remoteSelect ! SignedConsensusMessage(message, AuthenticationDigitsGenerator.generateMAC(message, sessionKey))

    case message@(_: StartChunkStream | _: DataChunk) => remoteSelect ! UnsignedMessage(message)

    case GetSessionKey  => sender() ! id -> sessionKey
  }

}

object RemoteReplicaActor {

  case class RequestSessionKey(senderId: Long)

  case object GetSessionKey

  case class SignedConsensusMessage(message: ConsensusMessage, mac: Mac)
  case class UnsignedMessage(message: Any)

}
