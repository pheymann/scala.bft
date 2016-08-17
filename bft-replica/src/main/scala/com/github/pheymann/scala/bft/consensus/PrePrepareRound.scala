package com.github.pheymann.scala.bft.consensus

class PrePrepareRound(implicit val consensusContext: ConsensusContext) extends ConsensusActor {

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

    case JoinConsensus =>

  }

}

object PrePrepareRound {

  case object StartConsensus
  case object JoinConsensus

  case class PrePrepare(sequenceNumber: Long, view: Long, requestDigits: Array[Byte]) extends ConsensusMessage

}
