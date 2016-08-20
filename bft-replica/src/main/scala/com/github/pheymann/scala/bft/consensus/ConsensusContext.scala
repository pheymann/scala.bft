package com.github.pheymann.scala.bft.consensus

import com.github.pheymann.scala.bft.util.ClientRequest

case class ConsensusContext(
                            sequenceNumber: Long,
                            view:           Long,
                            request:        ClientRequest,
                            requestDigits:  Array[Byte]
                           ) {

  val toLog = s"$sequenceNumber-$view-${requestDigits.mkString("")}"

}
