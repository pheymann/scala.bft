package com.github.pheymann.scala.bft.replica

import akka.actor.{ActorRef, ActorSystem, Props}
import com.github.pheymann.scala.bft.Configuration
import com.github.pheymann.scala.bft.consensus.ConsensusMessage
import com.github.pheymann.scala.bft.model.{ClientRequest, RequestDelivery}
import com.github.pheymann.scala.bft.replica.RemoteReplicaSenderActor.RemoteRefProvider
import com.github.pheymann.scala.bft.replica.Replicas.MissingReplicaSelfDataException
import com.github.pheymann.scala.bft.replica.messaging.ChunkDataStreamSender
import com.github.pheymann.scala.bft.util.LoggingUtil

trait Replicas extends LoggingUtil {

  def self: Replica

  def config: Configuration

  def routerRef: ActorRef
  def remoteRefProvider: RemoteRefProvider

  private[replica] def remoteSenderRefs:    Seq[ActorRef]
  private[replica] def remoteReceiverRefs:  Seq[ActorRef]

  /**
    * Sends the message to all [[RemoteReplicaSenderActor]]s, which will sign it and
    * forward it the message to the corresponding replica.
    *
    * @param message unsigned message
    */
  def sendMessage(message: ConsensusMessage) {
    for (remoteReplicaRef <- remoteSenderRefs)
      remoteReplicaRef ! message
  }

  /**
    * Sends the [[ClientRequest]] to all replicas via a chunked datastream.
    *
    * @param request client request
    */
  def sendRequest(request: ClientRequest) {
    val delivery = RequestDelivery(self.sequenceNumber, self.view, request)

    ChunkDataStreamSender.send(
      self.id,
      RequestDelivery(self.sequenceNumber, self.view, request),
      remoteSenderRefs
    )(
      config.bftConfig.messageChunkSize
    )

    info(s"request.send: ${delivery.toLog}")
  }

}

object Replicas {

  case object MissingReplicaSelfDataException extends Exception

  def apply(
            routerRef:          ActorRef,
            config:             Configuration,
            remoteRefProvider:  RemoteRefProvider = RemoteRefProvider
           )(
            implicit system: ActorSystem
           ): Replicas = {
    new ReplicasStatic(routerRef, remoteRefProvider, config)(StaticReplicaDiscoveryConfig.config)
  }

}

class ReplicasStatic(
                      val routerRef:          ActorRef,
                      val remoteRefProvider:  RemoteRefProvider = RemoteRefProvider,
                      val config:             Configuration
                    )(
                      discoveryConfig:  StaticReplicaDiscoveryConfig
                    )
                    (implicit system: ActorSystem) extends Replicas {

  private val replicaData = StaticReplicaDiscovery.loadReplicaData(discoveryConfig)
  private val selfDataOpt = replicaData.find(_.id == config.bftConfig.selfId)

  if (selfDataOpt.isEmpty) {
    error(s"self id: ${config.bftConfig.selfId} missing in replica hosts")
    throw MissingReplicaSelfDataException
  }

  val self = new Replica(selfDataOpt.get)

  private[replica] val remoteSenderRefs = replicaData
                                            .filterNot(_.id == config.bftConfig.selfId)
                                            .map { data =>
                                              system.actorOf(
                                                Props(new RemoteReplicaSenderActor(data, self.id, null)(remoteRefProvider)),
                                                RemoteReplicaSenderActor.name(data.id)
                                              )
                                            }

  private[replica] val remoteReceiverRefs = replicaData
                                              .filterNot(_.id == config.bftConfig.selfId)
                                              .map { data =>
                                                system.actorOf(
                                                  Props(new RemoteReplicaReceiverActor(data, self.id)(routerRef, config)),
                                                  RemoteReplicaReceiverActor.name(data.id)
                                                )
                                              }

}
