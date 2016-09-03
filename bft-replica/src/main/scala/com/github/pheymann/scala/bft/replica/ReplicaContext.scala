package com.github.pheymann.scala.bft.replica

import akka.actor.{ActorRef, ActorSystem}
import com.github.pheymann.scala.bft.storage.LogStorage

trait ReplicaContext {

  def messaging: Messaging

  def replicas: Replicas
  def storage:  LogStorage

}

object ReplicaContext {

  def apply(publisherRef: ActorRef, system: ActorSystem): ReplicaContext = {
    new ReplicaContextExtension(publisherRef)(system)
  }

}

class ReplicaContextExtension(publisherRef: ActorRef)
                             (implicit system: ActorSystem) extends ReplicaContext {

  override val messaging = Messaging(publisherRef, system)

  override val replicas  = Replicas(system)
  override val storage   = LogStorage(system)

}
