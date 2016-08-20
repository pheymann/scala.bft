package com.github.pheymann.scala.bft.consensus

import akka.actor.{ActorRef, ActorSystem}
import com.github.pheymann.scala.bft.replica.{Replica, ReplicaContextMock, ReplicasMock}
import com.github.pheymann.scala.bft.storage.LogStorageMock
import com.github.pheymann.scala.bft.util.{ClientRequest, RequestDigitsGenerator}

class ConsensusSpecContext(
                            val specSender: ActorRef,
                            val request:    ClientRequest,

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

  val collectors = new ConsensusCollectors(specSender)

  val logStorageMock = new LogStorageMock {
    val _logCollectorRef = collectors.logCollectorRef

    override def isWithinWatermarks(message: ConsensusMessage)    = logIsWithWatermarks
    override def hasAcceptedOrUnknown(message: ConsensusMessage)  = logHasAcceptedOrUnknown
  }

  val replicasMock = new ReplicasMock(new Replica(0L, view, sequenceNumber), collectors.roundCollectorRef)

  implicit val replicaContext = new ReplicaContextMock(replicasMock, logStorageMock)

}
