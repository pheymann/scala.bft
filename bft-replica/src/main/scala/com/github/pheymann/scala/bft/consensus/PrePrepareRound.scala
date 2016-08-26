package com.github.pheymann.scala.bft.consensus

import com.github.pheymann.scala.bft.replica.ReplicaContext
import com.github.pheymann.scala.bft.util.ClientRequest

class PrePrepareRound(
                       implicit
                       val consensusContext: ConsensusContext,
                       val replicaContext:   ReplicaContext
                     ) extends ConsensusRoundActor {

  import PrePrepareRound._

  protected val round = roundName

  protected val message = PrePrepare(
    replicas.self.id,
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

  private val roundName = "pre-prepare"

  case object StartConsensus
  case object JoinConsensus
  case object FinishedPrePrepare

  case class PrePrepare(
                         replicaId:       Long,
                         sequenceNumber:  Long,
                         view:            Long,
                         requestDigits:   Array[Byte]
                       ) extends ConsensusMessage

}
