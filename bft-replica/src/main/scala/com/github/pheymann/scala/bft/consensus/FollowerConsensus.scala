package com.github.pheymann.scala.bft.consensus

import akka.actor.ActorSystem
import akka.pattern.ask
import com.github.pheymann.scala.bft.consensus.PrePrepareRound.{JoinConsensus, PrePrepare}
import com.github.pheymann.scala.bft.replica.ReplicaContext
import com.github.pheymann.scala.bft.util.{ClientRequest, RequestDelivery}

case class FollowerConsensus(request: ClientRequest)
                            (implicit val system: ActorSystem, val replicaContext: ReplicaContext) extends ConsensusInstance(request) {

  import com.github.pheymann.scala.bft.BftReplicaConfig.consensusTimeout

  override def start() = instanceRef ? JoinConsensus

}

object FollowerConsensus {

  def createIfValid(message: PrePrepare, requestDelivery: RequestDelivery)
                   (implicit system: ActorSystem, replicaContext: ReplicaContext): Option[FollowerConsensus] = {
    if (
      message.sequenceNumber == requestDelivery.sequenceNumber &&
      message.view == requestDelivery.view &&
      replicaContext.storage.hasAcceptedOrUnknown(message)
    )
      Some(FollowerConsensus(requestDelivery.request))
    else
      None
  }

}
