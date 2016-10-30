package com.github.pheymann.scala.bft.replica

import java.security.MessageDigest

import akka.actor.ActorRef
import akka.util.Timeout
import com.github.pheymann.scala.bft.SessionKey

import scala.concurrent.duration.FiniteDuration

final case class ReplicaConfig(
                                id:             Int,
                                view:           Int,
                                expectedFaults: Int,

                                keyRequestDuration: FiniteDuration,

                                chunkSize:          Int,
                                digestStrategy:     String,
                                senderSessions:   Map[Int, SessionKey],
                                receiverSessions: Map[Int, SessionKey],
                                senderRef:        ActorRef
                              ) {

  import ReplicaConfig._

  val expectedPrepares  = calculateExpectedPrepares(expectedFaults)
  val expectedCommits   = calculateExpectedCommits(expectedFaults)

  implicit val keyRequestTimeout = Timeout(keyRequestDuration)

  val digestGenerator = MessageDigest.getInstance(digestStrategy)

}

object ReplicaConfig {

  private[replica] def calculateExpectedPrepares(expectedFaults: Int):  Int = expectedFaults * 2
  private[replica] def calculateExpectedCommits(expectedFaults: Int):   Int = expectedFaults * 2 + 1

}
