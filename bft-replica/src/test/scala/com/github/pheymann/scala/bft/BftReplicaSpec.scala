package com.github.pheymann.scala.bft

import akka.actor.{ActorSystem, Props}
import com.github.pheymann.scala.bft.consensus.ConsensusContext
import com.github.pheymann.scala.bft.replica.{Replica, ReplicaContextMock}
import com.github.pheymann.scala.bft.storage.LogStorageMock
import com.github.pheymann.scala.bft.util.{ClientRequest, RequestDigitsGenerator, RoundMessageCollectorActor, StorageMessageCollectorActor}
import org.specs2.mutable.{After, Specification}

trait BftReplicaSpec extends Specification {

  val testSequenceNumber  = 0L
  val testView            = 0L

  val testRequest       = ClientRequest()
  val testRequestDigits = RequestDigitsGenerator.generateDigits(testRequest)

  trait SpecContext extends After {

    implicit val consensusContext = ConsensusContext(
      testSequenceNumber,
      testView,
      testRequest,
      testRequestDigits
    )

    implicit val system = ActorSystem("test-system")

    def logStorageMock: LogStorageMock

    implicit val replicaContext = new ReplicaContextMock(new Replica(0L, testView), logStorageMock)

    override def after {
      system.terminate()
    }

  }

}
