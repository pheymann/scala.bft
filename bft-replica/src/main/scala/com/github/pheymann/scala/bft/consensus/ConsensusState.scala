package com.github.pheymann.scala.bft.consensus

import com.github.pheymann.scala.bft.messaging.ClientRequest

case class ConsensusState(
                            replicaId:      Int,
                            view:           Int,
                            sequenceNumber: Long,

                            lowWatermark:   Long,
                            highWatermark:  Long,

                            request:        ClientRequest
                         ) {

  var isPrePrepared = false
  var isPrepared    = false
  var isCommited    = false

  var receivedPrepares  = 0
  var receivedCommits   = 0

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
