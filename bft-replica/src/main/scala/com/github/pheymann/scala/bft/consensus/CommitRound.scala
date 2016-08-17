package com.github.pheymann.scala.bft.consensus

import com.github.pheymann.scala.bft.consensus.CommitRound.Commit
import com.github.pheymann.scala.bft.consensus.ConsensusRound.StartRound

class CommitRound(implicit val consensusContext: ConsensusContext) extends ConsensusRound {

  protected final val expectedMessages = 10 //TODO use 2f + 1

  protected val message = Commit(
    consensusContext.sequenceNumber,
    consensusContext.view,
    consensusContext.requestDigits
  )
  protected def executeMessage(message: ConsensusMessage): Unit = {
    storage.addCommit(message)
    //TODO execute operations
  }

}

object CommitRound {

  case object StartCommit extends StartRound

  case class Commit(sequenceNumber: Long, view: Long, requestDigits: Array[Byte]) extends ConsensusMessage

}
