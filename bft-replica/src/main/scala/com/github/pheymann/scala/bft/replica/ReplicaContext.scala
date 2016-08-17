package com.github.pheymann.scala.bft.replica

import akka.actor.{ActorSystem, ExtendedActorSystem, Extension, ExtensionId, ExtensionIdProvider}
import com.github.pheymann.scala.bft.storage.LogStorage

trait ReplicaContext extends Extension {

  def replicas: Replicas
  def storage:  LogStorage

}

object ReplicaContext extends ExtensionId[ReplicaContext]
                      with    ExtensionIdProvider {

  override def lookup = this

  override def createExtension(system: ExtendedActorSystem): ReplicaContext = new ReplicaContextService()(system)

}

class ReplicaContextService(implicit system: ActorSystem) extends ReplicaContext {

  val replicas  = Replicas(system)
  val storage   = LogStorage(system)

}
