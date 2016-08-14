package com.github.pheymann.scala.bft.consensus

import com.github.pheymann.scala.bft.util.{ClientRequest, ClientRequestDigest}

trait PrePrepareRound { this: Consensus =>

  import PrePrepareRound._

  def prePrepare: Receive = {
    case t =>
  }

}

object PrePrepareRound {

  case class PrePrepare(sequenceNumber: Long, view: Long, requestDigest: ClientRequestDigest)
  case class RequestDelivery(sequenceNumber: Long, view: Long, clientRequest: ClientRequest)

}
