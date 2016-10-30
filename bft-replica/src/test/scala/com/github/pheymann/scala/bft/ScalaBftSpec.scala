package com.github.pheymann.scala.bft

import akka.actor.ActorRef
import com.github.pheymann.scala.bft.consensus.ConsensusState
import com.github.pheymann.scala.bft.replica.ReplicaConfig
import org.specs2.mutable.Specification

import scala.concurrent.duration._

trait ScalaBftSpec extends Specification {

  val testDuration    = 5.seconds
  val testSessionKey  = (0 until 16).map(_.toByte).toArray
  val testChunkSize   = 5

  def newConfig(
                 replicaId: Int,
                 view:      Int,
                 expectedFaults:  Int,
                 senderRef:       ActorRef = null
               ): ReplicaConfig = {
    val sessionKeys = Map(replicaId -> testSessionKey)

    ReplicaConfig(replicaId, view, expectedFaults, null, testChunkSize, "MD5", sessionKeys, sessionKeys, senderRef)
  }

  def checkState(state: ConsensusState, roundIsTrue: String) = roundIsTrue match {
    case "pre-prepare" =>
      state.isPrePrepared should beTrue
      state.isPrepared should beFalse
      state.isCommited should beFalse
    case "prepare" =>
      state.isPrePrepared should beFalse
      state.isPrepared should beTrue
      state.isCommited should beFalse
    case "commit" =>
      state.isPrePrepared should beFalse
      state.isPrepared should beFalse
      state.isCommited should beTrue
    case _ =>
      state.isPrePrepared should beFalse
      state.isPrepared should beFalse
      state.isCommited should beFalse
  }

}
