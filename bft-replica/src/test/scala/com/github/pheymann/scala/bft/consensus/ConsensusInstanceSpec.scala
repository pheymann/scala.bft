package com.github.pheymann.scala.bft.consensus

import java.util.concurrent.TimeoutException

import akka.actor.ActorSystem
import com.github.pheymann.scala.bft.consensus.CommitRound.Commit
import com.github.pheymann.scala.bft.consensus.PrepareRound.Prepare
import com.github.pheymann.scala.bft.{BftReplicaConfig, BftReplicaSpec}
import com.github.pheymann.scala.bft.util._
import org.specs2.concurrent.ExecutionEnv

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

class ConsensusInstanceSpec extends BftReplicaSpec {

  """The Consensus Instance and its two implementations for Leaders and Followers is the atomic unit
    |of the protocol handling the three consensus protocol and the internal state. It
  """.stripMargin should {
    "reach a consensus if all rounds have passed for leader replicas" in { implicit ee: ExecutionEnv =>
      val request     = new ClientRequest(Array[Byte](0))
      val specContext = new ConsensusSpecContext(request)

      import specContext.replicaContext

      specContext.collectors.initCollectors(RoundMessageExpectation.forValidConsensus, StorageMessageExpectation.forValidConsensus)

      val consensus = new LeaderConsensus(request)

      testConsensus(consensus, specContext)
    }

    "reach a consensus if all rounds have passed for follower replicas" in { implicit ee: ExecutionEnv =>
      val request     = new ClientRequest(Array[Byte](1))
      val specContext = new ConsensusSpecContext(request)

      import specContext.replicaContext

      specContext.collectors.initCollectors(RoundMessageExpectation.forValidConsensus, StorageMessageExpectation.forValidConsensus)

      val consensus = new FollowerConsensus(request)

      testConsensus(consensus, specContext)
    }

    "not reach a consensus when not all round conditions are fulfilled" in { implicit ee: ExecutionEnv =>
      val request     = new ClientRequest(Array[Byte](2))
      val specContext = new ConsensusSpecContext(request)

      import specContext.replicaContext
      import system.dispatcher

      specContext.collectors.initCollectors(RoundMessageExpectation.forValidConsensus, StorageMessageExpectation.forValidConsensus)

      val consensus = new LeaderConsensus(request)

      val resultFut = Future {
        isConsensus(consensus)
      }

      for (index <- 0 until (2 * BftReplicaConfig.expectedFaultyReplicas))
        consensus.instanceRef ! Prepare(specContext.sequenceNumber, specContext.view, specContext.requestDigits)

      resultFut should beFalse.await(0, 10.seconds)
    }
  }

  def testConsensus(instance: ConsensusInstance, specContext: ConsensusSpecContext)
                   (implicit system: ActorSystem, ee: ExecutionEnv) = {
    import system.dispatcher

    val resultFut = Future {
      isConsensus(instance)
    }

    for (index <- 0 until (2 * BftReplicaConfig.expectedFaultyReplicas))
      instance.instanceRef ! Prepare(specContext.sequenceNumber, specContext.view, specContext.requestDigits)

    for (index <- 0 until (2 * BftReplicaConfig.expectedFaultyReplicas + 1))
      instance.instanceRef ! Commit(specContext.sequenceNumber, specContext.view, specContext.requestDigits)

    resultFut should beTrue.await(0, 10.seconds)
  }

  def isConsensus(instance: ConsensusInstance): Boolean = {
    var isConsensus = false

    try {
      Await.result(instance.start(), testDuration)
      isConsensus = true
    }
    catch {
      case _: TimeoutException => instance.logAborted()
    }
    isConsensus
  }

}
