package com.github.pheymann.scala.bft.replica

import java.util.concurrent.TimeUnit

import akka.util.Timeout
import com.typesafe.config.Config

import scala.concurrent.duration.{Duration, FiniteDuration}

final case class ReplicaConfig(
                                id:             Int,
                                expectedFaults: Int,

                                lowWatermark:   Long,
                                highWatermark:  Long,

                                keyRequestDuration: FiniteDuration,
                                messageDuration:    FiniteDuration,

                                chunkSize:          Int,
                                digestStrategy:     String
                              ) {

  import ReplicaConfig._

  val expectedPrepares  = calculateExpectedPrepares(expectedFaults)
  val expectedCommits   = calculateExpectedCommits(expectedFaults)

  implicit val keyRequestTimeout  = Timeout(keyRequestDuration)
  implicit val messageTimeout     = Timeout(messageDuration)

}

object ReplicaConfig {

  private[replica] def calculateExpectedPrepares(expectedFaults: Int):  Int = expectedFaults * 2
  private[replica] def calculateExpectedCommits(expectedFaults: Int):   Int = expectedFaults * 2 + 1

  private val configBaseKey = "scala.bft.replica"

  def fromConfig(id: Int, config: Config): ReplicaConfig = {
    ReplicaConfig(
      id,
      config.getInt(configBaseKey + ".expectedFaults"),
      config.getLong(configBaseKey + ".watermark.low"),
      config.getLong(configBaseKey + ".watermark.high"),
      Duration(config.getDuration(configBaseKey + ".sessionKey.timeout").toNanos, TimeUnit.NANOSECONDS),
      Duration(config.getDuration(configBaseKey + ".message.timeout").toNanos, TimeUnit.NANOSECONDS),
      config.getInt(configBaseKey + ".chunkSize"),
      config.getString(configBaseKey + ".digest.strategy")
    )
  }

}
