package com.github.pheymann.scala.bft

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import com.github.pheymann.scala.bft.consensus.ConsensusState
import com.github.pheymann.scala.bft.replica.{ReplicaConfig, ReplicaContext}
import org.specs2.mutable.{After, Specification}

import scala.concurrent.Await
import scala.concurrent.duration._

trait ScalaBftSpec extends Specification {

  val testDuration    = 5.seconds
  val testSessionKey  = (0 until 16).map(_.toByte).toArray
  val testChunkSize   = 5

  def newContext(
                  isLeader: Boolean,
                  view:     Int,

                  expectedFaults: Int
                ): ReplicaContext = {
    ReplicaContext(isLeader, view, 0L)(newConfig(0, expectedFaults))
  }

  def newConfig(
                 replicaId: Int,
                 expectedFaults:  Int
               ): ReplicaConfig = {
    ReplicaConfig(replicaId, expectedFaults, 0, 1, null, null, testChunkSize, "MD5")
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

  abstract class WithActorSystem  extends TestKit(ActorSystem())
                                  with    After
                                  with    ImplicitSender {

    override def after = {
      Await.result(system.terminate(), testDuration)
    }

  }

}
