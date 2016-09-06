package com.github.pheymann.scala.bft.consensus

import com.github.pheymann.scala.bft.Types.{Mac, RequestDigits}
import com.github.pheymann.scala.bft.model.ClientRequest

case class ConsensusContext(
                            sequenceNumber: Long,
                            view:           Long,
                            request:        ClientRequest,
                            requestDigits:  RequestDigits,
                            requestMacs:    Map[Long, Mac]
                           ) {

  val toLog = s"$sequenceNumber-$view-${requestDigits.mkString("")}"

}
