package com.github.pheymann.scala.bft.consensus

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import com.github.pheymann.scala.bft.consensus.CommitRound.{Commit, FinishedCommit, StartCommit}
import com.github.pheymann.scala.bft.consensus.PrePrepareRound.{FinishedPrePrepare, JoinConsensus, StartConsensus}
import com.github.pheymann.scala.bft.consensus.PrepareRound.{FinishedPrepare, Prepare, StartPrepare}
import com.github.pheymann.scala.bft.replica.ReplicaContext
import com.github.pheymann.scala.bft.util.{ActorLoggingUtil, ClientRequest, LoggingUtil, RequestDigitsGenerator}

import scala.concurrent.Future

abstract class ConsensusInstance(request: ClientRequest)
                                (implicit
                                  system:         ActorSystem,
                                  replicaContext: ReplicaContext
                                ) extends LoggingUtil {

  import ConsensusInstance._

  protected implicit val consensusContext = ConsensusContext(
    replicaContext.replicas.self.sequenceNumber,
    replicaContext.replicas.self.view,
    request,
    RequestDigitsGenerator.generateDigits(request)
  )

  class ConsensusInstanceActor  extends Actor
                                with    ActorLogging
                                with    ActorLoggingUtil {

    protected val prePrepareRound = createRound(Props(new PrePrepareRound()), "pre-prepare")
    protected val prepareRound    = createRound(Props(new PrepareRound()), "prepare")
    protected val commitRound     = createRound(Props(new CommitRound()), "commit")

    private def createRound(round: Props, name: String): ActorRef = {
      context.system.actorOf(round, s"$name-${consensusContext.toLog}")
    }

    private var _sender: ActorRef = null

    override def receive = {
      case StartConsensus =>
        _sender = sender()
        prePrepareRound ! StartConsensus
      case JoinConsensus =>
        _sender = sender()
        prePrepareRound ! JoinConsensus

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

  val instanceRef = system.actorOf(Props(new ConsensusInstanceActor()))

  def start(): Future[Any]

  def logAborted() {
    info(s"{${consensusContext.sequenceNumber},${consensusContext.view},[${consensusContext.requestDigits.mkString("")}]}.aborted")
  }

}

object ConsensusInstance {

  case object FinishedConsensus

}
