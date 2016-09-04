package com.github.pheymann.scala.bft.consensus

import akka.actor.{Actor, ActorRef, Props}
import com.github.pheymann.scala.bft.consensus.CommitRound.{Commit, FinishedCommit, StartCommit}
import com.github.pheymann.scala.bft.consensus.PrePrepareRound.{FinishedPrePrepare, JoinConsensus, StartConsensus}
import com.github.pheymann.scala.bft.consensus.PrepareRound.{FinishedPrepare, Prepare, StartPrepare}
import com.github.pheymann.scala.bft.replica.ReplicaContext
import com.github.pheymann.scala.bft.replica.messaging.MessageBrokerActor.ConsumeMessage
import com.github.pheymann.scala.bft.util.ActorLoggingUtil

class ConsensusInstanceActor()
                            (implicit
                              consensusContext: ConsensusContext,
                              replicaContext:   ReplicaContext
                            ) extends Actor
                              with    ActorLoggingUtil {

  import ConsensusInstanceActor._

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
    case ConsumeMessage =>
      replicaContext.messaging.messageBrokerRef ! ConsumeMessage
  }

}

object ConsensusInstanceActor {

  case object FinishedConsensus

}
