package com.github.pheymann.scala.bft

import org.slf4j.LoggerFactory
import org.specs2.mutable.{After, Specification}

import scala.concurrent.duration._

trait ScalaBftSpec extends Specification {

  val testDuration = 5.seconds

  abstract class WithLogger(name: String = "spec-logger") extends After {

    implicit val log = LoggerFactory.getLogger(name)

    override def after = ()

  }

}
