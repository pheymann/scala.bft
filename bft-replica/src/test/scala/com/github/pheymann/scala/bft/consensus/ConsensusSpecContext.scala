package com.github.pheymann.scala.bft.consensus

import akka.actor.{ActorRef, ActorSystem}
import com.github.pheymann.scala.bft.model.ClientRequest
import com.github.pheymann.scala.bft.replica.{Replica, ReplicaContextMock, ReplicasMock}
import com.github.pheymann.scala.bft.storage.LogStorageMock
import com.github.pheymann.scala.bft.util.RequestDigitsGenerator

class ConsensusSpecContext(
                            val specRef: ActorRef,
                            val request: ClientRequest,

                            val sequenceNumber: Long = 0L,
                            val view:           Long = 0L,

                            logIsWithWatermarks:     Boolean = true,
                            logHasAcceptedOrUnknown: Boolean = true
                          )
                          (implicit system: ActorSystem) {

  val requestDigits = RequestDigitsGenerator.generateDigits(request)

  implicit val consensusContext = ConsensusContext(
    sequenceNumber,
    view,
    request,
    requestDigits
  )

  val logStorageMock  = new LogStorageMock(specRef, logIsWithWatermarks, logHasAcceptedOrUnknown)
  val replicasMock    = new ReplicasMock(specRef, new Replica(0L, view, sequenceNumber))

  implicit val replicaContext = new ReplicaContextMock(replicasMock, logStorageMock, specRef)

}
