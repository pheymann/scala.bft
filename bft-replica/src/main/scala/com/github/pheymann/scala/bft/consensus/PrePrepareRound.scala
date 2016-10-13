package com.github.pheymann.scala.bft.consensus

import com.github.pheymann.scala.bft.replica.ReplicaContext
import com.github.pheymann.scala.bft.storage.LogStorageInterfaceActor.{AddPrePrepare, StartForRequest}

class PrePrepareRound(
                       implicit
                       val consensusContext: ConsensusContext,
                       val replicaContext:   ReplicaContext
                     ) extends ConsensusRoundActor {

  import PrePrepareRound._

  protected val round = roundName

  protected val message = PrePrepare(
    replicaContext.replicas.self.id,
    consensusContext.sequenceNumber,
    consensusContext.view
  )

  replicaContext.storageRef ! StartForRequest(consensusContext.request)
  replicaContext.storageRef ! AddPrePrepare(message)

  override def receive = {
    case StartConsensus =>
      replicaContext.replicas.sendMessage(message)
      replicaContext.replicas.sendRequest(consensusContext.request)

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
                          replicaId:      Long,
                          sequenceNumber: Long,
                          view:           Long
                       ) extends ConsensusMessage

}
