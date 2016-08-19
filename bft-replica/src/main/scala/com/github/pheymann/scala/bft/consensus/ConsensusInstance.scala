package com.github.pheymann.scala.bft.consensus

import java.util.concurrent.TimeoutException

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.github.pheymann.scala.bft.BftReplicaConfig._
import com.github.pheymann.scala.bft.consensus.CommitRound.{Commit, FinishedCommit, StartCommit}
import com.github.pheymann.scala.bft.consensus.PrePrepareRound.FinishedPrePrepare
import com.github.pheymann.scala.bft.consensus.PrepareRound.{FinishedPrepare, Prepare, StartPrepare}
import com.github.pheymann.scala.bft.replica.ReplicaContext
import com.github.pheymann.scala.bft.util.{ActorLoggingUtil, ClientRequest, RequestDigitsGenerator}

import scala.concurrent.{Await, Future}

trait ConsensusInstance extends Actor
                        with    ActorLogging
                        with    ActorLoggingUtil {

  import ConsensusInstance._

  implicit def replicaContext: ReplicaContext

  def request: ClientRequest

  protected implicit lazy val consensusContext = ConsensusContext(
    replicaContext.replicas.self.sequenceNumber,
    replicaContext.replicas.self.view,
    request,
    RequestDigitsGenerator.generateDigits(request)
  )

  protected lazy val prePrepareRound = createRound(Props(new PrePrepareRound()), "pre-prepare")
  protected lazy val prepareRound    = createRound(new PrepareRound(), "prepare")
  protected lazy val commitRound     = createRound(new CommitRound(), "commit")

  private def createRound(round: ConsensusRound, name: String): ActorRef = {
    createRound(Props(round), name)
  }

  private def createRound(round: Props, name: String): ActorRef = {
    context.system.actorOf(
      round,
      s"$name[${consensusContext.requestDigits.mkString("")}]"
    )
  }

  private val _sender = sender()

  override def receive = {
    case FinishedPrePrepare =>
      prepareRound ! StartPrepare
    case FinishedPrepare =>
      commitRound ! StartCommit
    case FinishedCommit =>
      _sender ! FinishedConsensus

    case prepareMessage: Prepare =>
      prepareRound ! prepareMessage
    case commitMessage: Commit =>
      commitRound ! commitMessage
  }

  protected def runConsensus: Future[Any]

  def consensus: Boolean = {
    var isConsensus = false

    try {
      Await.ready(runConsensus, timeoutDuration)
      isConsensus = true
    }
    catch {
      case _: TimeoutException =>
        info(s"[${consensusContext.requestDigits.mkString("")}].aborted")
    }
    finally {
      context.stop(self)
    }
    isConsensus
  }

}

object ConsensusInstance {

  case object FinishedConsensus

}
