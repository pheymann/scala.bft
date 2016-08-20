package com.github.pheymann.scala.bft.util

import akka.actor.{Actor, ActorLogging, ActorRef}

class CollectorStateObserver(specSender: ActorRef)  extends Actor
                                                    with    ActorLogging
                                                    with    ActorLoggingUtil {

  import CollectorStateObserver._

  private var isRoundReady  = false
  private var isLogReady    = false

  override def receive = {
    case RoundCollectorReady =>
      isRoundReady = true

      responseIfReady()

    case LogCollectorReady =>
      isLogReady = true

      responseIfReady()

  }

  private def responseIfReady() {
    if (isRoundReady && isLogReady) {
      debug("all collectors are ready")
      specSender ! CollectorsReady
    }
  }

}

object CollectorStateObserver {

  case object CollectorsReady

  case object RoundCollectorReady
  case object LogCollectorReady

}
