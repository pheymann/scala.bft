package com.github.pheymann.scala.bft

import akka.actor.{ActorRef, ActorSystem, Props}
import com.github.pheymann.scala.bft.util.CollectorStateObserver.{LogCollectorReady, RoundCollectorReady}
import com.github.pheymann.scala.bft.util.RoundMessageCollectorActor.InitRoundCollector
import com.github.pheymann.scala.bft.util.StorageMessageCollectorActor.InitStorageCollector
import com.github.pheymann.scala.bft.util._

class ConsensusCollectors(specSender: ActorRef)
                         (implicit system: ActorSystem) {

  val observerRef = system.actorOf(Props(new CollectorStateObserver(specSender)))

  val roundCollectorRef = system.actorOf(Props(new RoundMessageCollectorActor(observerRef)))
  val logCollectorRef   = system.actorOf(Props(new StorageMessageCollectorActor(observerRef)))

  def initCollectors(roundExpectation: RoundMessageExpectation, logExpectation: StorageMessageExpectation) {
    roundCollectorRef ! InitRoundCollector(roundExpectation)
    logCollectorRef   ! InitStorageCollector(logExpectation)
  }

  def setRoundCollectorReady() {
    observerRef ! RoundCollectorReady
  }

  def setLogCollectorReady() {
    observerRef ! LogCollectorReady
  }

}
