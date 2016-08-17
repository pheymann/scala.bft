package com.github.pheymann.scala.bft.consensus

import com.github.pheymann.scala.bft.consensus.ConsensusRound.StartRound
import com.github.pheymann.scala.bft.consensus.PrepareRound.Prepare

class PrepareRound(implicit val consensusContext: ConsensusContext) extends ConsensusRound {

  protected final val expectedMessages = 10 //TODO use 2f

  protected val message = Prepare(
    consensusContext.sequenceNumber,
    consensusContext.view,
    consensusContext.requestDigits
  )
  protected def executeMessage(message: ConsensusMessage): Unit = {
    storage.addPrepare(message)
  }

}

object PrepareRound {

  case object StartPrepare extends StartRound

  case class Prepare(sequenceNumber: Long, view: Long, requestDigits: Array[Byte]) extends ConsensusMessage

}
