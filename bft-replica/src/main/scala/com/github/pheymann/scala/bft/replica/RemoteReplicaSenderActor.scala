package com.github.pheymann.scala.bft.replica

import akka.actor.{Actor, ActorContext, ActorSelection}
import com.github.pheymann.scala.bft.Types._
import com.github.pheymann.scala.bft.consensus.ConsensusMessage
import com.github.pheymann.scala.bft.model.{DataChunk, StartChunkStream}
import com.github.pheymann.scala.bft.util.{ActorLoggingUtil, AuthenticationDigitsGenerator}

import RemoteReplicaSenderActor._

class RemoteReplicaSenderActor(
                                id:   Long,

                                host: String,
                                port: Int,

                                selfId:     Long,
                                sessionKey: SessionKey
                              )(
                                remoteRefProvider: RemoteRefProvider
                              ) extends Actor
                                with    ActorLoggingUtil {

  import RemoteReplicaSenderActor._

  def this(data: ReplicaData, selfId: Long, sessionKey: SessionKey)
          (remoteRefProvider: RemoteRefProvider) = this(data.id, data.host, data.port, selfId, sessionKey)(remoteRefProvider)

  private val remoteSelect = remoteRefProvider(id, host, port)(context)

  override def receive = {
    case message: ConsensusMessage =>
      remoteSelect ! SignedConsensusMessage(message, AuthenticationDigitsGenerator.generateMAC(message, sessionKey))

    case message@(_: StartChunkStream | _: DataChunk) => remoteSelect ! UnsignedMessage(message)
  }

}

object RemoteReplicaSenderActor {

  case class SignedConsensusMessage(message: ConsensusMessage, mac: Mac)
  case class UnsignedMessage(message: Any)

  def name(replicaId: Long): String = s"remote-replica-sender-$replicaId"

  type RemoteRefProvider = ((Long, String, Int) => (ActorContext => ActorSelection))

  val RemoteRefProvider = new RemoteRefProvider {
    override def apply(id: Long, host: String, port: Int): ActorContext => ActorSelection = { (context: ActorContext) =>
      context.actorSelection(s"akka.tcp://scala-bft-replica@$host:$port/user/${RemoteReplicaReceiverActor.name(id)}")
    }
  }

}
