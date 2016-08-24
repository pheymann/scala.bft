package com.github.pheymann.scala.bft.util

import org.slf4j.LoggerFactory

trait LoggingUtil {

  private lazy val log = LoggerFactory.getLogger(getClass)

  def info(msg: String) {
    if (log.isInfoEnabled())
      log.info(msg)
  }

  def error(cause: Throwable, msg: => String) {
    log.error(msg, cause)
  }

}
