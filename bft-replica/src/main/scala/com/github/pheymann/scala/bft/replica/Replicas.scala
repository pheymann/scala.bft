package com.github.pheymann.scala.bft.replica

import akka.actor.{ActorSystem, ExtendedActorSystem, Extension, ExtensionId, ExtensionIdProvider}
import com.github.pheymann.scala.bft.consensus.ConsensusMessage
import com.github.pheymann.scala.bft.util.ClientRequest

trait Replicas extends Extension {

  def sendMessage(message: ConsensusMessage): Unit
  def sendRequest(message: ConsensusMessage, request: ClientRequest): Unit

}

object Replicas extends ExtensionId[Replicas] with ExtensionIdProvider {

  override def lookup = this

  override def createExtension(system: ExtendedActorSystem): Replicas = new ReplicasNetwork()(system)

}

class ReplicasNetwork(implicit system: ActorSystem) extends Replicas {

  override def sendMessage(message: ConsensusMessage): Unit = {
    //TODO implement send message
  }

  override def sendRequest(message: ConsensusMessage, request: ClientRequest): Unit = {
    //TODO implement send request
  }

}
