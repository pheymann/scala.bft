package com.github.pheymann.scala.bft.consensus

import akka.actor.{ActorRef, ActorSystem}
import com.github.pheymann.scala.bft.Types.Mac
import com.github.pheymann.scala.bft.{SpecContext, Types}
import com.github.pheymann.scala.bft.model.ClientRequest
import com.github.pheymann.scala.bft.util.AuthenticationDigitsGenerator

class ConsensusSpecContext(
                            request: ClientRequest
                          )(
                            specRef: ActorRef,

                            sequenceNumber: Long = 0L,
                            view:           Long = 0L,

                            logIsWithWatermarks:     Boolean = true,
                            logHasAcceptedOrUnknown: Boolean = true
                          )
                          (implicit
                            system: ActorSystem
                          ) {

  val context = new SpecContext(specRef, sequenceNumber, view, logIsWithWatermarks, logHasAcceptedOrUnknown)

  val requestDigits = AuthenticationDigitsGenerator.generateDigits(request)

  implicit val consensusContext = ConsensusContext(
    context.sequenceNumber,
    context.view,
    request,
    requestDigits,
    Map.empty[Long, Mac]
  )

}
