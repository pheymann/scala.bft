package com.github.pheymann.scala.bft.replica

import akka.actor.{ActorRef, ActorSystem, ExtendedActorSystem, Extension, ExtensionId, ExtensionIdProvider, Props}
import com.github.pheymann.scala.bft.BftReplicaConfig
import com.github.pheymann.scala.bft.consensus.ConsensusMessage
import com.github.pheymann.scala.bft.model.{ClientRequest, RequestDelivery}
import com.github.pheymann.scala.bft.replica.Replicas.MissingReplicaSelfDataException
import com.github.pheymann.scala.bft.util.LoggingUtil

trait Replicas extends Extension with LoggingUtil {

  def self: Replica

  private[replica] def remoteReplicaRefs: Seq[ActorRef]

  def sendMessage(message: ConsensusMessage) {
    for (remoteReplicaRef <- remoteReplicaRefs)
      remoteReplicaRef ! message
  }

  def sendRequest(request: ClientRequest) {
    ChunkDataStreamSender.send(self.id, RequestDelivery(self.sequenceNumber, self.view, request), remoteReplicaRefs)

    info(s"request.send: {${request.clientId},${request.timestamp}}{${self.id},${self.sequenceNumber},${self.view}}")
  }

}

object Replicas extends ExtensionId[Replicas] with ExtensionIdProvider {

  override def lookup = this

  override def createExtension(system: ExtendedActorSystem): Replicas = new ReplicasStatic()(system)

  case object MissingReplicaSelfDataException extends Exception

}

class ReplicasStatic(implicit system: ActorSystem) extends Replicas {

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
