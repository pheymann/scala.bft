package com.github.pheymann.scala.bft.replica

import akka.actor.{ActorSystem, ExtendedActorSystem, Extension, ExtensionId, ExtensionIdProvider}

trait Replicas extends Extension {

  def sendMessage(message: Any): Unit
  def sendRequest(message: Any): Unit

}

object Replicas extends ExtensionId[Replicas] with ExtensionIdProvider {

  override def lookup = this

  override def createExtension(system: ExtendedActorSystem): Replicas = new ReplicasNetwork()(system)

}

class ReplicasNetwork(implicit system: ActorSystem) extends Replicas {

  override def sendMessage(message: Any): Unit = {
    //TODO implement send message
  }

  override def sendRequest(message: Any): Unit = {
    //TODO implement send request
  }

}
