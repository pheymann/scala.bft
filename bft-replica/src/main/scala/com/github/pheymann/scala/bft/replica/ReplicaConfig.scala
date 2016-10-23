package com.github.pheymann.scala.bft.replica

import akka.util.Timeout

import scala.concurrent.duration.FiniteDuration

final case class ReplicaConfig(
                                id:             Int,
                                view:           Int,
                                expectedFaults: Int,

                                keyRequestDuration: FiniteDuration
                              ) {

  import ReplicaConfig._

  val expectedPrepares  = calculateExpectedPrepares(expectedFaults)
  val expectedCommits   = calculateExpectedCommits(expectedFaults)

  implicit val keyRequestTimeout = Timeout(keyRequestDuration)

}

object ReplicaConfig {

  private[replica] def calculateExpectedPrepares(expectedFaults: Int): Int = expectedFaults * 2
  private[replica] def calculateExpectedCommits(expectedFaults: Int): Int = expectedFaults * 2 + 1

}
