package com.github.pheymann.scala.bft

import java.util.concurrent.TimeUnit

import akka.util.Timeout
import com.typesafe.config.ConfigFactory

object BftReplicaConfig {

  private val config = ConfigFactory.load().getConfig("bft.replica")

  val expectedFaultyReplicas = config.getInt("expected.faulty-replicas")

  val lowWatermark  = config.getInt("water-mark.low")
  val highWatermark = config.getInt("water-mark.high")

  import scala.concurrent.duration._

  val timeoutDuration = FiniteDuration(config.getDuration("consensus-timeout").toNanos, TimeUnit.NANOSECONDS)
  implicit val consensusTimeout = Timeout(timeoutDuration)

}
