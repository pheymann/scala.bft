package com.github.pheymann.scala.bft.util

import akka.actor.{Actor, ActorLogging}

trait ActorLoggingUtil extends ActorLogging { this: Actor =>

  def logWarn(msg: => String): Unit = {
    if (log.isWarningEnabled)
      log.warning(msg)
  }

  def logInfo(msg: => String): Unit = {
    if (log.isInfoEnabled)
      log.info(msg)
  }

}
