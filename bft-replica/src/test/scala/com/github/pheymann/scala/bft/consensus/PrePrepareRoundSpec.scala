package com.github.pheymann.scala.bft.consensus

import cats._
import com.github.pheymann.scala.bft.ScalaBftSpec
import com.github.pheymann.scala.bft.messaging.{ClientRequest, PrePrepareMessage, PrepareMessage}
import com.github.pheymann.scala.bft.replica.{ReplicaAction, ReplicaConfig}
import com.github.pheymann.scala.bft.storage.{StorePrePrepare, StorePrepare}
import com.github.pheymann.scala.bft.util.ScalaBftLogger
import org.slf4j.Logger

class PrePrepareRoundSpec extends ScalaBftSpec {

  class SpecProcessor(implicit config: ReplicaConfig, log: Logger) extends (ReplicaAction ~> Id) {

    override def apply[A](action: ReplicaAction[A]): Id[A] = action match {
      case SendPrePrepareMessage(state) =>
        ScalaBftLogger.infoLog("spec: received command - send pre-prepare")
        PrePrepareMessage(0, 0, 0L)

      case SendPrepareMessage(state) =>
        ScalaBftLogger.infoLog("spec: received command - send prepare")
        PrepareMessage(0, 0, 0L)

      case SendClientRequest(request) => ScalaBftLogger.infoLog("spec: received command - send client request")

      case StorePrePrepare(_, _) => ScalaBftLogger.infoLog("spec: received command - store client request and pre-prepared")
      case StorePrepare(_) => ScalaBftLogger.infoLog("spec: received command - store prepared")
    }

  }

  "The pre-prepare round" should {
    """send a pre-prepare message, ClientRequest and afterwards the prepare message to all replicas if
      |it is the leader and store them also in the log
    """.stripMargin in new WithLogger("commit-round-spec") {
      implicit val config = newConfig(0, 0, 0)

      val processor = new SpecProcessor()
      val state     = ConsensusState(0, 0, 0, 0, 1)
      val request   = ClientRequest(0, 0L, Array.empty)

      PrePrepareRound.processLeaderPrePrepare(request, state).foldMap(processor).isPrePrepared should beTrue
      PrePrepareRound.processLeaderPrePrepare(request, state).foldMap(processor).isPrepared should beTrue
    }
  }

}
