package com.github.pheymann.scala.bft

import java.util.concurrent.TimeUnit

import akka.util.Timeout
import com.typesafe.config.ConfigFactory

object BftReplicaConfig {

  val messageRouterName = "message.router"

  private val config = ConfigFactory.load().getConfig("bft")

  val selfId = config.getLong("self.id")

  val messageChunkSize = config.getInt("message.chunk-size")

  private val bftConfig = config.getConfig("replica")

  val replicaHostFile = bftConfig.getString("hosts-file")

  val expectedFaultyReplicas = bftConfig.getInt("expected.faulty-replicas")

  val lowWatermark  = bftConfig.getInt("water-mark.low")
  val highWatermark = bftConfig.getInt("water-mark.high")

  val signatureStrategy = bftConfig.getString("signature.strategy")

  import scala.concurrent.duration._

  val consensusDuration = FiniteDuration(bftConfig.getDuration("timeout.consensus").toNanos, TimeUnit.NANOSECONDS)
  implicit val consensusTimeout = Timeout(consensusDuration)

  val keyRetrievalDuration = FiniteDuration(bftConfig.getDuration("timeout.key-retrieval").toNanos, TimeUnit.NANOSECONDS)
  implicit val keyRetrievalTimeout = Timeout(keyRetrievalDuration)

}
