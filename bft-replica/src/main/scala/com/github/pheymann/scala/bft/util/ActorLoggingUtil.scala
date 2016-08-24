package com.github.pheymann.scala.bft.util

import akka.actor.{Actor, ActorLogging}

trait ActorLoggingUtil extends ActorLogging { this: Actor =>

  def info(msg: => String) {
    if (log.isInfoEnabled)
      log.info(msg)
  }

  def debug(msg: => String) {
    if (log.isDebugEnabled)
      log.debug(msg)
  }

}
