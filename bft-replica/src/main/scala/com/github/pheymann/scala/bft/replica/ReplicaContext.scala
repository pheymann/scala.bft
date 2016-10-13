package com.github.pheymann.scala.bft.replica

import akka.actor.ActorRef
import com.github.pheymann.scala.bft.Configuration
import com.github.pheymann.scala.bft.replica.messaging.Messaging

trait ReplicaContext {

  def config: Configuration

  def messaging: Messaging

  def replicas: Replicas

  def storageRef:  ActorRef

}

object ReplicaContext {

  def apply(
             config: Configuration,
             messaging: Messaging,
             replicas: Replicas,
             storageRef: ActorRef
           ): ReplicaContext = {
    ReplicaContextExtension(config, messaging, replicas, storageRef)
  }

}

case class ReplicaContextExtension(
                                     config: Configuration,
                                     messaging: Messaging,
                                     replicas: Replicas,
                                     storageRef: ActorRef
                                   ) extends ReplicaContext

//val config: Configuration  = Configuration(),
//val messaging: Messaging   = Messaging(publisherRef, system),
//val replicas: Replicas     = Replicas(messaging.router, config),
//val storageRef: ActorRef   = system.actorOf(Props(new LogStorageInterfaceActor(null)), LogStorageInterfaceActor.name)
