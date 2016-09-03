package com.github.pheymann.scala.bft.consensus

import akka.actor.Props
import com.github.pheymann.scala.bft.{BftReplicaConfig, BftReplicaSpec, WithActorSystem}
import com.github.pheymann.scala.bft.consensus.CommitRound.{Commit, FinishedCommit, StartCommit}
import com.github.pheymann.scala.bft.consensus.PrepareRound.{FinishedPrepare, Prepare, StartPrepare}
import com.github.pheymann.scala.bft.model.ClientRequest
import com.github.pheymann.scala.bft.replica.MessageBrokerActor.ConsumeMessage
import com.github.pheymann.scala.bft.replica.ReplicasMock.CalledSendMessage
import com.github.pheymann.scala.bft.storage.LogStorageMock.{CalledAddCommit, CalledAddPrepare, CalledFinish}

class ConsensusRoundSpec extends BftReplicaSpec {

  sequential

  val expectedMsgPrepare  = 2 * BftReplicaConfig.expectedFaultyReplicas
  val expectedMsgCommit   = 2 * BftReplicaConfig.expectedFaultyReplicas + 1

  """The two different implementations of ConsensusRound are tested in this Spec.
    |
    |A consensus round""".stripMargin should {
    "(Prepare|Commit Round) distribute the current round message to all replicas on start" in new WithActorSystem {
      val request     = new ClientRequest(0, 0, Array[Byte](0))
      val specContext = new ConsensusSpecContext(request)(self)

      import specContext.consensusContext
      import specContext.context.replicaContext

      val commitRound = system.actorOf(Props(new CommitRound()))

      within(testDuration) {
        commitRound ! StartCommit

        expectMsg(CalledSendMessage)
        expectMsg(ConsumeMessage)
        expectNoMsg(noMessageDuration)
      }
    }

    "(Prepare Round) reach consensus when 2f messages are received" in new WithActorSystem {
      val request     = new ClientRequest(0, 0, Array[Byte](1))
      val specContext = new ConsensusSpecContext(request)(self)

      import specContext.consensusContext
      import specContext.context.replicaContext

      val prepareRound = system.actorOf(Props(new PrepareRound()))

      within(testDuration) {
        prepareRound ! StartPrepare

        expectMsg(CalledSendMessage)
        expectMsg(ConsumeMessage)

        for (index <- 0 until expectedMsgPrepare)
          prepareRound ! Prepare(index, specContext.context.sequenceNumber, specContext.context.view, specContext.requestDigits)

        expectMsgAllOf(Seq(CalledAddPrepare, FinishedPrepare) ++ Seq.fill(expectedMsgPrepare)(ConsumeMessage): _*)

        expectNoMsg(noMessageDuration)
      }
    }

    "(Commit Round) reach consensus when 2f + 1 messages are received" in new WithActorSystem {
      val request     = new ClientRequest(0, 0, Array[Byte](2))
      val specContext = new ConsensusSpecContext(request)(self)

      import specContext.consensusContext
      import specContext.context.replicaContext

      val commitRound = system.actorOf(Props(new CommitRound()))

      within(testDuration) {
        commitRound ! StartCommit

        expectMsg(CalledSendMessage)
        expectMsg(ConsumeMessage)

        for (index <- 0 until expectedMsgCommit)
          commitRound ! Commit(index, specContext.context.sequenceNumber, specContext.context.view, specContext.requestDigits)

        expectMsgAllOf(Seq(CalledAddCommit, CalledFinish, FinishedCommit) ++ Seq.fill(expectedMsgCommit)(ConsumeMessage): _*)

        expectNoMsg(noMessageDuration)
      }
    }

    "(Prepare|Commit Round) and just ignore additional messages" in new WithActorSystem {
      val request     = new ClientRequest(0, 0, Array[Byte](3))
      val specContext = new ConsensusSpecContext(request)(self)

      import specContext.consensusContext
      import specContext.context.replicaContext

      val commitRound = system.actorOf(Props(new CommitRound()))

      within(testDuration) {
        commitRound ! StartCommit

        expectMsg(CalledSendMessage)
        expectMsg(ConsumeMessage)

        for (index <- 0 until expectedMsgCommit)
          commitRound ! Commit(index, specContext.context.sequenceNumber, specContext.context.view, specContext.requestDigits)

        expectMsgAllOf(Seq(CalledAddCommit, CalledFinish, FinishedCommit) ++ Seq.fill(expectedMsgCommit)(ConsumeMessage): _*)

        commitRound ! Commit(0, specContext.context.sequenceNumber, specContext.context.view, specContext.requestDigits)
        expectMsg(ConsumeMessage)

        expectNoMsg(noMessageDuration)
      }
    }

    "(Prepare|Commit Round) finish directly on start if the consensus was already found" in new WithActorSystem {
      val request     = new ClientRequest(0, 0, Array[Byte](4))
      val specContext = new ConsensusSpecContext(request)(self)

      import specContext.consensusContext
      import specContext.context.replicaContext

      val commitRound = system.actorOf(Props(new CommitRound()))

      within(testDuration) {
        for (index <- 0 until (2 * BftReplicaConfig.expectedFaultyReplicas + 1))
          commitRound ! Commit(index, specContext.context.sequenceNumber, specContext.context.view, specContext.requestDigits)

        expectMsgAllOf(Seq.fill(expectedMsgCommit)(ConsumeMessage): _*)

        commitRound ! StartCommit

        expectMsg(CalledSendMessage)
        expectMsg(CalledAddCommit)
        expectMsg(CalledFinish)
        expectMsg(FinishedCommit)
        expectMsg(ConsumeMessage)

        expectNoMsg(noMessageDuration)
      }
    }
  }

}
