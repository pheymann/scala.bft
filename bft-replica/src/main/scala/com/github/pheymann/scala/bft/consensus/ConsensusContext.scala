package com.github.pheymann.scala.bft.consensus

import com.github.pheymann.scala.bft.model.ClientRequest

case class ConsensusContext(
                            sequenceNumber: Long,
                            view:           Long,
                            request:        ClientRequest
                           ) {

  lazy val toLog        = s"{$sequenceNumber,$view,${request.toLog}}"
  lazy val toActorName  = s"$sequenceNumber-${view}_${request.toActorName}"

}
