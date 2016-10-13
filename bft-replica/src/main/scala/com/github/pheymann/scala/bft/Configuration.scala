package com.github.pheymann.scala.bft

import java.util.concurrent.TimeUnit

import akka.util.Timeout
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration.FiniteDuration

case class Configuration(
                          bftConfig:      BftConfig     = BftConfig.config,
                          replicaConfig:  ReplicaConfig = ReplicaConfig.config
                        )

case class BftConfig(
                      selfId: Long,

                      messageChunkSize: Int
                    )

object BftConfig {

  private val bftConfig = ConfigFactory.load().getConfig("bft")

  val config = BftConfig(
    bftConfig.getLong("self.id"),
    bftConfig.getInt("message.chunk-size")
  )

}

case class ReplicaConfig(
                      expectedFaultyReplicas: Int,

                      lowWatermark:   Int,
                      highWatermark:  Int,

                      signatureStrategy: String,

                      consensusDuration:    FiniteDuration,
                      keyRetrievalDuration: FiniteDuration,
                      storageDuration:      FiniteDuration
                    ) {

  implicit val consensusTimeout     = Timeout(consensusDuration)
  implicit val keyRetrievalTimeout  = Timeout(keyRetrievalDuration)
  implicit val storageTimeout       = Timeout(storageDuration)

}

object ReplicaConfig {

  private val replicaConfig = ConfigFactory.load().getConfig("bft.replica")

  val config = ReplicaConfig(
    replicaConfig.getInt("expected.faulty-replicas"),
    replicaConfig.getInt("water-mark.low"),
    replicaConfig.getInt("water-mark.high"),
    replicaConfig.getString("signature.strategy"),
    FiniteDuration(replicaConfig.getDuration("timeout.consensus").toNanos, TimeUnit.NANOSECONDS),
    FiniteDuration(replicaConfig.getDuration("timeout.key-retrieval").toNanos, TimeUnit.NANOSECONDS),
    FiniteDuration(replicaConfig.getDuration("timeout.storage").toNanos, TimeUnit.NANOSECONDS)
  )

}
