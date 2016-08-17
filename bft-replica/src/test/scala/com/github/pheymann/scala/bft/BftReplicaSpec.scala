package com.github.pheymann.scala.bft

import akka.actor.{ActorSystem, Props}
import akka.util.Timeout
import com.github.pheymann.scala.bft.consensus.ConsensusContext
import com.github.pheymann.scala.bft.replica.{Replica, ReplicaContextMock, ReplicasMock}
import com.github.pheymann.scala.bft.storage.LogStorageMock
import com.github.pheymann.scala.bft.util._
import org.specs2.mutable.{After, Specification}

import scala.concurrent.duration._

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

    val timeoutDuration   = 5.second
    implicit val timeout  = Timeout(timeoutDuration)
    implicit val system = ActorSystem("test-system")

    def roundExpectation:   RoundMessageExpectation
    def storageExpectation: StorageMessageExpectation

    val roundCollectorRef = system.actorOf(Props(new RoundMessageCollectorActor(roundExpectation)))
    val logCollectorRef   = system.actorOf(Props(new StorageMessageCollectorActor(storageExpectation)))

    def logStorageMock: LogStorageMock
    val replicasMock = new ReplicasMock(new Replica(0L, testView), roundCollectorRef)

    implicit val replicaContext = new ReplicaContextMock(replicasMock, logStorageMock)

    val roundObserver = new CollectorStateObserver(roundCollectorRef)
    val logObserver   = new CollectorStateObserver(logCollectorRef)

    override def after {
      system.terminate()
    }

  }

}
