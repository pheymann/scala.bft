package com.github.pheymann.scala.bft

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.util.Timeout
import com.github.pheymann.scala.bft.util.RoundMessageCollectorActor.InitRoundCollector
import com.github.pheymann.scala.bft.util.StorageMessageCollectorActor.InitStorageCollector
import com.github.pheymann.scala.bft.util._

import scala.concurrent.Await
import scala.concurrent.duration._

class ConsensusCollectors(implicit system: ActorSystem) {

  val roundCollectorRef = system.actorOf(Props(new RoundMessageCollectorActor()))
  val logCollectorRef   = system.actorOf(Props(new StorageMessageCollectorActor()))

  val roundObserver = createObserver(roundCollectorRef)
  val logObserver   = createObserver(logCollectorRef)

  private def createObserver(collectorRef: ActorRef)
                            (implicit system: ActorSystem): CollectorStateObserver = {
    import ConsensusCollectors.timeout

    new CollectorStateObserver(collectorRef)(timeout, system)
  }

  def initCollectors(roundExpectation: RoundMessageExpectation, logExpectation: StorageMessageExpectation) {
    roundCollectorRef ! InitRoundCollector(roundExpectation)
    logCollectorRef   ! InitStorageCollector(logExpectation)
  }

  // when returning the expectations are fulfilled in the collectors
  def observedResult(duration: Duration = ConsensusCollectors.timeoutDuration) = {
    import system.dispatcher

    val result = for {
      roundValid  <- roundObserver.checkCollector
      logValid    <- logObserver.checkCollector
    } yield roundValid && logValid

    Await.result(result, duration)
  }

}

object ConsensusCollectors {

  private val timeoutDuration = 5.seconds
  private implicit val timeout = Timeout(timeoutDuration)

}
