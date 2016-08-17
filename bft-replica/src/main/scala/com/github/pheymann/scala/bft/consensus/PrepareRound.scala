package com.github.pheymann.scala.bft.consensus

import com.github.pheymann.scala.bft.BftReplicaConfig
import com.github.pheymann.scala.bft.consensus.ConsensusRound.StartRound

class PrepareRound(implicit val consensusContext: ConsensusContext) extends ConsensusRound {

  import PrepareRound._

  protected final val expectedMessages = 2 * BftReplicaConfig.expectedFaultyReplicas

  protected val message = Prepare(
    consensusContext.sequenceNumber,
    consensusContext.view,
    consensusContext.requestDigits
  )
  protected def executeMessage(message: ConsensusMessage) {
    storage.addPrepare(message)
    sender() ! FinishedPrepare
  }

}

object PrepareRound {

  case object StartPrepare extends StartRound
  case object FinishedPrepare

  case class Prepare(sequenceNumber: Long, view: Long, requestDigits: Array[Byte]) extends ConsensusMessage

}
