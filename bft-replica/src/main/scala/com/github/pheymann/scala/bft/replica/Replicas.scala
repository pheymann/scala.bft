package com.github.pheymann.scala.bft.replica

import akka.actor.{ActorRef, ActorSystem, ExtendedActorSystem, Extension, ExtensionId, ExtensionIdProvider, Props}
import com.github.pheymann.scala.bft.BftReplicaConfig
import com.github.pheymann.scala.bft.consensus.ConsensusMessage
import com.github.pheymann.scala.bft.replica.Replicas.MissingReplicaSelfDataException
import com.github.pheymann.scala.bft.util.{ClientRequest, DataChunk, LoggingUtil, RequestDelivery}

trait Replicas extends Extension {

  def self: Replica

  private[replica] def remoteReplicaRefs: Seq[ActorRef]

  def sendMessage(message: ConsensusMessage) {
    for (remoteReplicaRef <- remoteReplicaRefs)
      remoteReplicaRef ! message
  }

  def sendRequest(message: ConsensusMessage, request: ClientRequest) {
    RequestDelivery
      .marshall(RequestDelivery(message.sequenceNumber, message.view, request))
      .grouped(BftReplicaConfig.messageChunkSize)
      .zipWithIndex
      .foreach { case (chunk, index) =>
        for (remoteReplicaRef <- remoteReplicaRefs)
          remoteReplicaRef ! DataChunk(index, chunk)
      }
  }

}

object Replicas extends ExtensionId[Replicas] with ExtensionIdProvider {

  override def lookup = this

  override def createExtension(system: ExtendedActorSystem): Replicas = new ReplicasStatic()(system)

  case object MissingReplicaSelfDataException extends Exception

}

class ReplicasStatic(implicit system: ActorSystem) extends Replicas with LoggingUtil {

  private val selfOpt = StaticReplicaDiscovery.replicaData.find(_.id == BftReplicaConfig.selfId)

  if (selfOpt.isEmpty) {
    error(s"self id: ${BftReplicaConfig.selfId} missing in ${BftReplicaConfig.replicaHostFile}")
    throw MissingReplicaSelfDataException
  }

  val self = new Replica(selfOpt.get)

  private[replica] val remoteReplicaRefs = StaticReplicaDiscovery.replicaData
                                            .filterNot(_.id == BftReplicaConfig.selfId)
                                            .map { data =>
                                              system.actorOf(Props(new RemoteReplicaActor(data)))
                                            }

}
