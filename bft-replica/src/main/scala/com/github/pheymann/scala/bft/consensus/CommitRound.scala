package com.github.pheymann.scala.bft.consensus

import com.github.pheymann.scala.bft.BftReplicaConfig
import com.github.pheymann.scala.bft.consensus.ConsensusRound.StartRound
import com.github.pheymann.scala.bft.replica.ReplicaContext

class CommitRound(
                  implicit
                  val consensusContext: ConsensusContext,
                  val replicaContext:   ReplicaContext
                 ) extends ConsensusRound {

  import CommitRound._

  protected val round = roundName

  protected final val expectedMessages = 2 * BftReplicaConfig.expectedFaultyReplicas + 1

  protected val message = Commit(
    replicas.self.id,
    consensusContext.sequenceNumber,
    consensusContext.view,
    consensusContext.requestDigits
  )
  protected def executeMessage(message: ConsensusMessage) {
    storage.addCommit(message)
    storage.finishForRequest(message)
    sender() ! FinishedCommit
  }

}

object CommitRound {

  private val roundName = "commit"

  case object StartCommit extends StartRound
  case object FinishedCommit

  case class Commit(
                     replicaId:       Long,
                     sequenceNumber:  Long,
                     view:            Long,
                     requestDigits:   Array[Byte]
                   ) extends ConsensusMessage

}
