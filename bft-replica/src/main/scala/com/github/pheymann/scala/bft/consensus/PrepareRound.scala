package com.github.pheymann.scala.bft.consensus

import com.github.pheymann.scala.bft.{BftReplicaConfig, Types}
import com.github.pheymann.scala.bft.Types.{Mac, RequestDigits}
import com.github.pheymann.scala.bft.consensus.ConsensusRound.StartRound
import com.github.pheymann.scala.bft.replica.ReplicaContext

class PrepareRound(
                    implicit
                    val consensusContext: ConsensusContext,
                    val replicaContext:   ReplicaContext
                  ) extends ConsensusRound {

  import PrepareRound._

  protected val round = roundName

  protected final val expectedMessages = 2 * BftReplicaConfig.expectedFaultyReplicas

  protected val message = Prepare(
    replicas.self.id,
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

  private val roundName = "prepare"

  case object StartPrepare extends StartRound
  case object FinishedPrepare

  case class Prepare(
                      replicaId:      Long,
                      sequenceNumber: Long,
                      view:           Long,
                      requestDigits:  RequestDigits,

                      requestMac:     Mac = Types.EmptyMac
                    ) extends ConsensusMessage {
    override def withMac(requestMac: Mac): ConsensusMessage = copy(requestMac = requestMac)

  }

}
