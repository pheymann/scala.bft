package com.github.pheymann.scala.bft

import akka.actor.{ActorRef, ActorSystem}
import com.github.pheymann.scala.bft.replica.Replica

class SpecContext(
                    val specRef: ActorRef,

                    val sequenceNumber: Long = 0L,
                    val view:           Long = 0L,

                    val logIsWithWatermarks:     Boolean = true,
                    val logHasAcceptedOrUnknown: Boolean = true
                 )( implicit
                    system: ActorSystem
                 ) {

//  val logStorageMock  = new LogStorageMock(specRef, logIsWithWatermarks, logHasAcceptedOrUnknown)
//  val replicasMock    = new ReplicasMock(specRef, new Replica(0L, view, sequenceNumber))
//
//  implicit val replicaContext = new ReplicaContextMock(replicasMock, logStorageMock, specRef)

}
