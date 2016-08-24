package com.github.pheymann.scala.bft.replica

import akka.actor.{ActorRef, ActorSystem, ExtendedActorSystem, Extension, ExtensionId, ExtensionIdProvider, Props}
import com.github.pheymann.scala.bft.BftReplicaConfig
import com.github.pheymann.scala.bft.consensus.ConsensusMessage
import com.github.pheymann.scala.bft.util.ClientRequest

trait Replicas extends Extension {

  def self: Replica

  private[replica] def remoteReplicaRefs: Seq[ActorRef]

  def sendMessage(message: ConsensusMessage): Unit
  def sendRequest(message: ConsensusMessage, request: ClientRequest): Unit

}

object Replicas extends ExtensionId[Replicas] with ExtensionIdProvider {

  override def lookup = this

  override def createExtension(system: ExtendedActorSystem): Replicas = new ReplicasStatic()(system)

}

class ReplicasStatic(implicit system: ActorSystem) extends Replicas {

  private val selfOpt = StaticReplicaDiscovery.replicaData.find(_.id == BftReplicaConfig.selfId)

  require(selfOpt.isDefined, s"self id: ${BftReplicaConfig.selfId} missing in ${BftReplicaConfig.replicaHostFile}")

  val self = new Replica(selfOpt.get)

  private[replica] val remoteReplicaRefs = StaticReplicaDiscovery.replicaData
                                            .filter(_.id == BftReplicaConfig.selfId)
                                            .map { data =>
                                              system.actorOf(Props(new RemoteReplicaActor(data)))
                                            }

  override def sendMessage(message: ConsensusMessage) {
    //TODO implement send message
  }

  override def sendRequest(message: ConsensusMessage, request: ClientRequest) {
    //TODO implement send request
  }

}
