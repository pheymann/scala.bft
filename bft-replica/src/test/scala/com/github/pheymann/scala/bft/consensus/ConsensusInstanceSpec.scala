package com.github.pheymann.scala.bft.consensus

import com.github.pheymann.scala.bft.consensus.CommitRound.Commit
import com.github.pheymann.scala.bft.consensus.PrePrepareRound.PrePrepare
import com.github.pheymann.scala.bft.consensus.PrepareRound.Prepare
import com.github.pheymann.scala.bft.model.{ClientRequest, RequestDelivery}
import com.github.pheymann.scala.bft._
import com.github.pheymann.scala.bft.replica.{ReplicaContext, Replicas}
import com.github.pheymann.scala.bft.replica.messaging.Messaging
import org.specs2.concurrent.ExecutionEnv

import scala.concurrent.Future
import scala.concurrent.blocking

class ConsensusInstanceSpec(implicit ee: ExecutionEnv) extends BftReplicaSpec {

  sequential

  """The Consensus Instance and its two implementations for Leaders and Followers is the atomic unit
    |of the protocol handling the three consensus protocol and the internal state. It
  """.stripMargin should {
    "reach a consensus if all rounds have passed (leader)" in new WithActorSystem {

      val config    = Configuration()
      val messaging = Messaging(null, system)
      val replicas  = Replicas(messaging.router, config, (_, _, _) => (_) => system.actorSelection(self.path))


      implicit val replicaContext = ReplicaContext(config, messaging, replicas, self)

      val request   = new ClientRequest(0, 0, Array[Byte](0))
      val consensus = new LeaderConsensus()

      within(testDuration * 2) {
        val resultFut = Future(blocking(consensus ? request))

        sendTestMessages()

        expectMsg("hello")

        expectNoMsg(noMessageDuration)

        resultFut should beTrue.awaitFor(testDuration * 2)
      }
    }

//    "reach a consensus if all rounds have passed (follower)" in new WithActorSystem {
//      val specContext = new SpecContext(self, 1)
//
//      val request         = new ClientRequest(0, 0, Array[Byte](1))
//      val message         = PrePrepare(0, 0, 0)
//      val requestDelivery = RequestDelivery(0, 0, request)
//
//      import specContext.replicaContext
//
//      val consensus = new FollowerConsensus()
//
//      within(testDuration * 2) {
//        val resultFut = Future(blocking(consensus ? (message, requestDelivery)))
//
//        sendTestMessages(specContext)
//
//        expectMsg(CalledStart)
//        expectMsg(CalledAddPrePrepare)
//        expectMsg(CalledSendMessage)
//        expectMsg(CalledAddPrepare)
//        expectMsg(CalledSendMessage)
//        expectMsg(CalledAddCommit)
//        expectMsg(CalledFinish)
//
//        expectNoMsg(noMessageDuration)
//
//        resultFut should beTrue.awaitFor(testDuration)
//      }
//    }
//
//    "not reach a consensus when not all round conditions are fulfilled" in new WithActorSystem {
//      val specContext = new SpecContext(self, 1)
//
//      val request = new ClientRequest(0, 0, Array[Byte](2))
//
//      import specContext.replicaContext
//      import system.dispatcher
//
//      val consensus = new LeaderConsensus()
//
//      val resultFut = Future(blocking(consensus ? request))
//
//      for (index <- 0 until (2 * BftReplicaConfig.expectedFaultyReplicas)) {
//        specContext.replicaContext.messaging.messageBrokerRef ! Prepare(
//          0L,
//          specContext.sequenceNumber,
//          specContext.view
//        )
//      }
//
//      resultFut should beFalse.awaitFor(testDuration * 2)
//    }
  }

  def sendTestMessages()(implicit replicaContext: ReplicaContext): Unit = {
    for (index <- 0 until (2 * replicaContext.config.replicaConfig.expectedFaultyReplicas)) {
      replicaContext.messaging.messageBrokerRef ! Prepare(
        index,
        replicaContext.replicas.self.sequenceNumber,
        replicaContext.replicas.self.view
      )
    }

    for (index <- 0 until (2 * replicaContext.config.replicaConfig.expectedFaultyReplicas + 1)) {
      replicaContext.messaging.messageBrokerRef ! Commit(
        index,
        replicaContext.replicas.self.sequenceNumber,
        replicaContext.replicas.self.view
      )
    }
  }

}
