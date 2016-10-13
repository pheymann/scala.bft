package com.github.pheymann.scala.bft.consensus

import akka.pattern.ask
import akka.actor.{ActorRef, ActorSystem, Props}
import com.github.pheymann.scala.bft.consensus.ConsensusInstanceActor.FinishedConsensus
import com.github.pheymann.scala.bft.consensus.PrePrepareRound.{JoinConsensus, PrePrepare, StartConsensus}
import com.github.pheymann.scala.bft.model.{ClientRequest, RequestDelivery}
import com.github.pheymann.scala.bft.replica.ReplicaContext
import com.github.pheymann.scala.bft.replica.messaging.MessageBrokerActor.NewConsensusInstance
import com.github.pheymann.scala.bft.storage.LogStorageInterfaceActor
import com.github.pheymann.scala.bft.util.LoggingUtil

import scala.concurrent.{Await, Future}
import scala.util.control.NonFatal

abstract class ConsensusInstance()
                                (implicit
                                  system:         ActorSystem,
                                  replicaContext: ReplicaContext
                                ) extends LoggingUtil {

  protected def runConsensus(request: ClientRequest): Boolean = {
    implicit val consensusContext = ConsensusContext(
      replicaContext.replicas.self.sequenceNumber,
      replicaContext.replicas.self.view,
      request
    )

    info(s"consensus.start: ${consensusContext.toLog}")

    val instanceRef = system.actorOf(Props(new ConsensusInstanceActor()))

    // forward messages to the current consensus instance
    replicaContext.messaging.messageBrokerRef ! NewConsensusInstance(instanceRef)

    try {
      Await.result(startConsensus(instanceRef), replicaContext.config.replicaConfig.consensusDuration) match {
        case FinishedConsensus => true
        case other =>
          error(s"${logAborted(request)}: $other")
          false
      }
    }
    catch {
      case NonFatal(cause) =>
        error(cause, logAborted(request))
        false
    }
  }

  protected def startConsensus(instanceRef: ActorRef): Future[Any]

  private def logAborted(request: ClientRequest): String = {
    "{%d,%d,%s}.aborted".format(
      replicaContext.replicas.self.sequenceNumber,
      replicaContext.replicas.self.view,
      request.toLog
    )
  }

}

case class LeaderConsensus()
                          (implicit system: ActorSystem, replicaContext: ReplicaContext) extends ConsensusInstance {

  private val config = replicaContext.config

  import config.replicaConfig.consensusTimeout

  override protected def startConsensus(instanceRef: ActorRef) = instanceRef ? StartConsensus

  def ? (request: ClientRequest): Boolean = runConsensus(request)

}

case class FollowerConsensus()
                            (implicit system: ActorSystem, replicaContext: ReplicaContext) extends ConsensusInstance {

  private val config = replicaContext.config

  import config.replicaConfig.consensusTimeout

  override protected def startConsensus(instanceRef: ActorRef) = instanceRef ? JoinConsensus

  def ? (message: PrePrepare, requestDelivery: RequestDelivery): Boolean = {
    val isValid = {
      message.sequenceNumber == requestDelivery.sequenceNumber &&
        message.view == requestDelivery.view &&
        LogStorageInterfaceActor.hasAcceptedOrUnknown(message)
    }

    if (isValid)
      runConsensus(requestDelivery.request)
    else {
      error(s"request.invalid: ${requestDelivery.toLog},${message.toLog}")
      false
    }
  }

}
