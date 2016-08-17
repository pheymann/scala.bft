package com.github.pheymann.scala.bft.util

import akka.actor.ActorLogging

trait ActorLoggingUtil { this: ActorLogging =>

  def debug(msg: => String): Unit = {
    if (log.isDebugEnabled)
      log.debug(msg)
  }

}