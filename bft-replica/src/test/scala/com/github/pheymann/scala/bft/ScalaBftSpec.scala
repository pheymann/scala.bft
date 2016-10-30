package com.github.pheymann.scala.bft

import com.github.pheymann.scala.bft.consensus.ConsensusState
import com.github.pheymann.scala.bft.replica.ReplicaConfig
import org.specs2.mutable.Specification

import scala.concurrent.duration._

trait ScalaBftSpec extends Specification {

  val testDuration = 5.seconds

  def newConfig(replicaId: Int, view: Int, expectedFaults: Int): ReplicaConfig = {
    ReplicaConfig(replicaId, view, expectedFaults, null, "MD5")
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
