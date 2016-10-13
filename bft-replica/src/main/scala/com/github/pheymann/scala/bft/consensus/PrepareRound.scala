package com.github.pheymann.scala.bft.consensus

import com.github.pheymann.scala.bft.consensus.ConsensusRound.StartRound
import com.github.pheymann.scala.bft.replica.ReplicaContext
import com.github.pheymann.scala.bft.storage.LogStorageInterfaceActor.AddPrepare

class PrepareRound(
                    implicit
                    val consensusContext: ConsensusContext,
                    val replicaContext:   ReplicaContext
                  ) extends ConsensusRound {

  import PrepareRound._

  protected val round = roundName

  protected final val expectedMessages = 2 * replicaContext.config.replicaConfig.expectedFaultyReplicas

  protected val message = Prepare(
    replicaContext.replicas.self.id,
    consensusContext.sequenceNumber,
    consensusContext.view
  )
  protected def executeMessage(message: ConsensusMessage) {
    replicaContext.storageRef ! AddPrepare(message)
    sender() ! FinishedPrepare
  }

}

object PrepareRound {

  private val roundName = "prepare"

  case object StartPrepare extends StartRound
  case object FinishedPrepare

  case class Prepare(
                      replicaId:      Long,
                      sequenceNumber: Long,
                      view:           Long
                    ) extends ConsensusMessage

}
