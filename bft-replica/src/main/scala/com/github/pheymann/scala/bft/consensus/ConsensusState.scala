package com.github.pheymann.scala.bft.consensus

import org.slf4j.{Logger, LoggerFactory}

case class ConsensusState(
                            replicaId:      Int,
                            view:           Int,
                            sequenceNumber: Long,

                            lowWatermark:   Long,
                            highWatermark:  Long
                         ) {

  var isPrePrepared = false
  var isPrepared    = false
  var isCommited    = false

  var receivedPrepares  = 0
  var receivedCommits   = 0

  implicit val log = LoggerFactory.getLogger(s"{$replicaId,$view,$sequenceNumber}")

}

object ConsensusState {

  def toLog(state: ConsensusState): String = {
    "{%d,%d,%d}[%b,%b,%b]".format(
      state.replicaId,
      state.sequenceNumber,
      state.view,
      state.isPrePrepared,
      state.isPrepared,
      state.isCommited
    )
  }

}
