package com.github.pheymann.scala.bft.util

import org.slf4j.Logger

trait ScalaBftLogger {

  def errorLog(msg: => String)
              (implicit log: Logger): Unit = {
    log.error(msg)
  }

  def errorLog(cause: Throwable, msg: => String)
              (implicit log: Logger): Unit = {
    log.error(msg, cause)
  }

  def infoLog(msg: => String)
             (implicit log: Logger): Unit = {
    if (log.isInfoEnabled)
      log.info(msg)
  }

  def debugLog(msg: => String)
              (implicit log: Logger): Unit = {
    if (log.isDebugEnabled)
      log.debug(msg)
  }

}

object ScalaBftLogger extends ScalaBftLogger
