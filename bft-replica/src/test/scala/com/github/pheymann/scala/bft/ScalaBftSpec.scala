package com.github.pheymann.scala.bft

import com.github.pheymann.scala.bft.replica.ReplicaConfig
import org.slf4j.LoggerFactory
import org.specs2.mutable.{After, Specification}

import scala.concurrent.duration._

trait ScalaBftSpec extends Specification {

  val testDuration = 5.seconds

  def newConfig(replicaId: Int, view: Int, expectedFaults: Int): ReplicaConfig = {
    ReplicaConfig(replicaId, view, expectedFaults, null, "MD5")
  }

}
