package com.github.pheymann.scala.bft

import akka.actor.{ActorSystem, Props}
import akka.util.Timeout
import com.github.pheymann.scala.bft.consensus.{ConsensusContext, ConsensusMessage}
import com.github.pheymann.scala.bft.replica.{Replica, ReplicaContextMock, ReplicasMock}
import com.github.pheymann.scala.bft.storage.LogStorageMock
import com.github.pheymann.scala.bft.util.RoundMessageCollectorActor.InitRoundCollector
import com.github.pheymann.scala.bft.util.StorageMessageCollectorActor.InitStorageCollector
import com.github.pheymann.scala.bft.util._
import org.specs2.mutable.{After, Specification}

import scala.concurrent.Await
import scala.concurrent.duration._

trait BftReplicaSpec extends Specification {

  val testRequest       = ClientRequest()
  val testRequestDigits = RequestDigitsGenerator.generateDigits(testRequest)

  val timeoutDuration   = 5.second
  implicit val timeout  = Timeout(timeoutDuration)

  trait SpecContext extends After {

    var testSequenceNumber  = 0L
    val testView            = 0L

    implicit lazy val consensusContext = ConsensusContext(
      testSequenceNumber,
      testView,
      testRequest,
      testRequestDigits
    )

    implicit val system = ActorSystem("test-system")

    val roundCollectorRef = system.actorOf(Props(new RoundMessageCollectorActor()))
    val logCollectorRef   = system.actorOf(Props(new StorageMessageCollectorActor()))

    val logStorageMock = new LogStorageMock {
      val _logCollectorRef = logCollectorRef

      override def isWithinWatermarks(message: ConsensusMessage) = true
      override def hasAcceptedOrUnknown(message: ConsensusMessage) = true
    }
    val replicasMock = new ReplicasMock(new Replica(0L, testView, testSequenceNumber), roundCollectorRef)

    implicit lazy val replicaContext = new ReplicaContextMock(replicasMock, logStorageMock)

    val roundObserver = new CollectorStateObserver(roundCollectorRef)
    val logObserver   = new CollectorStateObserver(logCollectorRef)

    override def after {
      system.terminate()
    }

    def initCollectors(roundExpectation: RoundMessageExpectation, logExpectation: StorageMessageExpectation) {
      roundCollectorRef ! InitRoundCollector(roundExpectation)
      logCollectorRef   ! InitStorageCollector(logExpectation)
    }

    // when returning the expectations are fulfilled in the collectors
    def observedResult(implicit timeout: Timeout) = {
      import system.dispatcher

      val result = for {
        roundValid  <- roundObserver.checkCollector
        logValid    <- logObserver.checkCollector
      } yield roundValid && logValid

      Await.result(result, timeoutDuration)
    }

  }

}
