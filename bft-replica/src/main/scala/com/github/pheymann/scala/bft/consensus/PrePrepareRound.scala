package com.github.pheymann.scala.bft.consensus

import com.github.pheymann.scala.bft.replica.ReplicaContext
import com.github.pheymann.scala.bft.util.ClientRequest

class PrePrepareRound(
                       implicit
                       val consensusContext: ConsensusContext,
                       val replicaContext:   ReplicaContext
                     ) extends ConsensusRoundActor {

  import PrePrepareRound._

  private val message = PrePrepare(
    consensusContext.sequenceNumber,
    consensusContext.view,
    consensusContext.requestDigits
  )

  storage.startForRequest(consensusContext.request)
  storage.addPrePrepare(message)

  override def receive = {
    case StartConsensus =>
      replicas.sendMessage(message)
      replicas.sendRequest(message, consensusContext.request)

      sender() ! FinishedPrePrepare

    case JoinConsensus =>
      sender() ! FinishedPrePrepare
  }

}

object PrePrepareRound {

  case object StartConsensus
  case object JoinConsensus
  case object FinishedPrePrepare

  case class PrePrepare(sequenceNumber: Long, view: Long, requestDigits: Array[Byte]) extends ConsensusMessage
  case class RequestDelivery(request: ClientRequest, sequenceNumber: Long, view: Long)

}
