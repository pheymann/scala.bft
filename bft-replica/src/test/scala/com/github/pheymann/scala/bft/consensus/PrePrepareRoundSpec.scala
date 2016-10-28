package com.github.pheymann.scala.bft.consensus

import cats._
import com.github.pheymann.scala.bft.ScalaBftSpec
import com.github.pheymann.scala.bft.messaging.{ClientRequest, PrePrepareMessage, PrepareMessage}
import com.github.pheymann.scala.bft.replica.{ReplicaAction, ReplicaConfig}
import com.github.pheymann.scala.bft.storage.{StorePrePrepare, StorePrepare}
import com.github.pheymann.scala.bft.util.ScalaBftLogger
import org.slf4j.Logger

class PrePrepareRoundSpec extends ScalaBftSpec {

  val specProcessor = new (ReplicaAction ~> Id) {
    def apply[A](action: ReplicaAction[A]): Id[A] = action match {
      case SendPrePrepareMessage(state) => PrePrepareMessage(0, 0, 0L)
      case SendPrepareMessage(state)    => PrepareMessage(0, 0, 0L)
      case SendClientRequest(request)   => ()

      case StorePrePrepare(_, _)  => ()
      case StorePrepare(_)        => ()
    }
  }

  "The pre-prepare round" should {
    """send a pre-prepare message, ClientRequest and afterwards the prepare message to all replicas if
      |it is the leader and store them also in the log
    """.stripMargin in {
      val state     = ConsensusState(0, 0, 0, 0, 1)
      val request   = ClientRequest(0, 0L, Array.empty)

      val resultState = PrePrepareRound.processLeaderPrePrepare(request, state).foldMap(specProcessor)

      resultState.isPrePrepared should beTrue
      resultState.isPrepared should beTrue
    }

    "send a prepare message to all other replicas if it recevies a pre-prepare from the leader" in {
      val state       = ConsensusState(0, 0, 0, 0, 1)
      val resultState = PrePrepareRound.processFollowerPrePrepare(state).foldMap(specProcessor)

      resultState.isPrePrepared should beTrue
      resultState.isPrepared should beTrue
    }
  }

}
