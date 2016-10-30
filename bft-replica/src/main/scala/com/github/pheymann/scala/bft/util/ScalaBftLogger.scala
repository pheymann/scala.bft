package com.github.pheymann.scala.bft.util

import org.slf4j.Logger

trait ScalaBftLogger {

  def logError(msg: => String)
              (implicit log: Logger): Unit = {
    log.error(msg)
  }

  def logError(cause: Throwable, msg: => String)
              (implicit log: Logger): Unit = {
    log.error(msg, cause)
  }

  def logWarn(msg: => String)
             (implicit log: Logger): Unit = {
    if (log.isWarnEnabled)
      log.warn(msg)
  }

  def logInfo(msg: => String)
             (implicit log: Logger): Unit = {
    if (log.isInfoEnabled)
      log.info(msg)
  }

  def logDebug(msg: => String)
              (implicit log: Logger): Unit = {
    if (log.isDebugEnabled)
      log.debug(msg)
  }

}

object ScalaBftLogger extends ScalaBftLogger
