package com.github.pheymann.scala.bft.util

import akka.actor.{Actor, ActorLogging}

trait ActorLoggingUtil extends ActorLogging { this: Actor =>

  def infoLog(msg: => String): Unit = {
    if (log.isInfoEnabled)
      log.info(msg)
  }

}
