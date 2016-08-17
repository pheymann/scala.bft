package com.github.pheymann.scala.bft

import com.typesafe.config.ConfigFactory

object BftReplicaConfig {

  private val config = ConfigFactory.load().getConfig("bft.replica")

  val expectedFaultyReplicas = config.getInt("expected.faulty-replicas")

  val lowWatermark  = config.getInt("water-mark.low")
  val highWatermark = config.getInt("water-mark.high")

}
