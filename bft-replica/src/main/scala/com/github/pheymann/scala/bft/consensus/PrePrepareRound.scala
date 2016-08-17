package com.github.pheymann.scala.bft.consensus

import com.github.pheymann.scala.bft.consensus.PrepareRound.StartPrepare
import com.github.pheymann.scala.bft.util.ClientRequest

trait PrePrepareRound { this: Consensus with PrepareRound =>

  import PrePrepareRound._

  def prePrepare: Receive = {
    case StartConsensus =>
      replicas.sendMessage(PrePrepare(sequenceNumber, view, requestDigits))
      replicas.sendRequest(RequestDelivery(sequenceNumber, view, request))

      storage.store(PrePrepare(sequenceNumber, view, requestDigits))

      context.become(prepare)
      self ! StartPrepare
  }

}

object PrePrepareRound {

  case object StartConsensus

  case class PrePrepare(sequenceNumber: Long, view: Long, requestDigest: Array[Byte])
  case class RequestDelivery(sequenceNumber: Long, view: Long, clientRequest: ClientRequest)

}
