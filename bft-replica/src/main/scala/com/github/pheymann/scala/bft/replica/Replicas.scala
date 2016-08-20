package com.github.pheymann.scala.bft.replica

import akka.actor.{ActorSystem, ExtendedActorSystem, Extension, ExtensionId, ExtensionIdProvider}
import com.github.pheymann.scala.bft.consensus.ConsensusMessage
import com.github.pheymann.scala.bft.util.ClientRequest

trait Replicas extends Extension {

  def self: Replica

  def sendMessage(message: ConsensusMessage): Unit
  def sendRequest(message: ConsensusMessage, request: ClientRequest): Unit

}

object Replicas extends ExtensionId[Replicas] with ExtensionIdProvider {

  override def lookup = this

  override def createExtension(system: ExtendedActorSystem): Replicas = new ReplicasNetwork()(system)

}

class ReplicasNetwork(implicit system: ActorSystem) extends Replicas {

  //TODO determine correct self replica reference
  val self = new Replica(0l, 0l, 0l)

  override def sendMessage(message: ConsensusMessage) {
    //TODO implement send message
  }

  override def sendRequest(message: ConsensusMessage, request: ClientRequest) {
    //TODO implement send request
  }

}
