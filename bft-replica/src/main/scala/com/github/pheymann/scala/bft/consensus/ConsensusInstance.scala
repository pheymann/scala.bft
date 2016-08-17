package com.github.pheymann.scala.bft.consensus

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.github.pheymann.scala.bft.consensus.CommitRound.{Commit, FinishedCommit, StartCommit}
import com.github.pheymann.scala.bft.consensus.PrePrepareRound.FinishedPrePrepare
import com.github.pheymann.scala.bft.consensus.PrepareRound.{FinishedPrepare, Prepare, StartPrepare}
import com.github.pheymann.scala.bft.util.{ActorLoggingUtil, ClientRequest, RequestDigitsGenerator}

trait ConsensusInstance extends Actor
                        with    ActorLogging
                        with    ActorLoggingUtil {

  import ConsensusInstance._

  def request: ClientRequest

  //TODO get sequence number
  //TODO get view

  protected implicit val consensusContext = ConsensusContext(
    0L,
    0L,
    request,
    RequestDigitsGenerator.generateDigits(request)
  )

  protected val prePrepareRound = createRound(Props(new PrePrepareRound()), "pre-prepare")
  protected val prepareRound    = createRound(new PrepareRound(), "prepare")
  protected val commitRound     = createRound(new CommitRound(), "commit")

  private def createRound(round: ConsensusRound, name: String): ActorRef = {
    createRound(Props(round), name)
  }

  private def createRound(round: Props, name: String): ActorRef = {
    context.system.actorOf(
      round,
      s"$name[${consensusContext.requestDigits.toString}]"
    )
  }

  private val _sender = sender()

  def consensusReceive: Receive = {
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

}

object ConsensusInstance {

  case object FinishedConsensus

}
