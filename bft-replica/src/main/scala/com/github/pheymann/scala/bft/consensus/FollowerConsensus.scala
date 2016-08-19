package com.github.pheymann.scala.bft.consensus

import akka.pattern.ask
import com.github.pheymann.scala.bft.consensus.PrePrepareRound.{JoinConsensus, PrePrepare, RequestDelivery}
import com.github.pheymann.scala.bft.replica.ReplicaContext
import com.github.pheymann.scala.bft.util.ClientRequest

case class FollowerConsensus(request: ClientRequest)
                            (implicit val replicaContext: ReplicaContext) extends ConsensusInstance {

  import com.github.pheymann.scala.bft.BftReplicaConfig.consensusTimeout

  override def runConsensus = prePrepareRound ? JoinConsensus

}

object FollowerConsensus {

  def createIfValid(message: PrePrepare, requestDelivery: RequestDelivery)
                   (implicit replicaContext: ReplicaContext): Option[FollowerConsensus] = {
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
