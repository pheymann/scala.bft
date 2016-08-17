package com.github.pheymann.scala.bft.consensus

import com.github.pheymann.scala.bft.consensus.PrePrepareRound.{JoinConsensus, PrePrepare, RequestDelivery}
import com.github.pheymann.scala.bft.replica.ReplicaContext
import com.github.pheymann.scala.bft.util.{ClientRequest, RequestDigitsGenerator}

case class FollowerConsensus(implicit val replicaContext: ReplicaContext) extends ConsensusInstance {

  var request: ClientRequest = _

  private var messageOpt: Option[PrePrepare] = None
  private var requestDeliverOpt: Option[RequestDelivery] = None

  override def receive = {
    case message: PrePrepare =>
      messageOpt = Some(message)

      startConsensusIfReady()

    case requestDelivery: RequestDelivery =>
      requestDeliverOpt = Some(requestDelivery)

      startConsensusIfReady()
  }

  private def startConsensusIfReady() {
    messageOpt.foreach { message =>
      requestDeliverOpt.foreach { requestDelivery =>
        if (
          message.sequenceNumber == requestDelivery.sequenceNumber &&
          message.view == requestDelivery.view
        ) {
          val requestDigits = RequestDigitsGenerator.generateDigits(requestDelivery.request)

          if (message.requestDigits.sameElements(requestDigits)) {
            request = requestDelivery.request

            prePrepareRound ! JoinConsensus
          }
        }
      }
    }
  }

}
