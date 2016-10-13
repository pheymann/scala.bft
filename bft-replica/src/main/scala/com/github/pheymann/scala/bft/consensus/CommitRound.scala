package com.github.pheymann.scala.bft.consensus

import com.github.pheymann.scala.bft.consensus.ConsensusRound.StartRound
import com.github.pheymann.scala.bft.replica.ReplicaContext
import com.github.pheymann.scala.bft.storage.LogStorageInterfaceActor.{AddCommit, FinishForRequest}

class CommitRound(
                  implicit
                  val consensusContext: ConsensusContext,
                  val replicaContext:   ReplicaContext
                 ) extends ConsensusRound {

  import CommitRound._

  protected val round = roundName

  protected final val expectedMessages = 2 * replicaContext.config.replicaConfig.expectedFaultyReplicas + 1

  protected val message = Commit(
    replicaContext.replicas.self.id,
    consensusContext.sequenceNumber,
    consensusContext.view
  )
  protected def executeMessage(message: ConsensusMessage) {
    replicaContext.storageRef ! AddCommit(message)
    replicaContext.storageRef ! FinishForRequest(message)
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
                     view:            Long
                   ) extends ConsensusMessage

}
