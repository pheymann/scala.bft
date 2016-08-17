package com.github.pheymann.scala.bft.util

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

class CollectorStateObserver(collectorRef: ActorRef)
                            (implicit timeout: Timeout, system: ActorSystem) {

  import system.dispatcher
  import CollectorStateObserver._

  def checkCollector: Future[Boolean] = Future {
    while (!Await.result(collectorRef ? CheckState, 1.second).asInstanceOf[Boolean]) {
      Thread.sleep(500)
    }
    true
  }

}

object CollectorStateObserver {

  case object CheckState

}
