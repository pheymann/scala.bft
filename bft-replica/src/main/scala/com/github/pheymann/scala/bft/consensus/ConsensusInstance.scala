package com.github.pheymann.scala.bft.consensus

import akka.pattern.ask
import akka.actor.{ActorRef, ActorSystem, Props}
import com.github.pheymann.scala.bft.BftReplicaConfig
import com.github.pheymann.scala.bft.Types.{Mac, RequestDigits, SessionKey}
import com.github.pheymann.scala.bft.consensus.ConsensusInstanceActor.FinishedConsensus
import com.github.pheymann.scala.bft.consensus.PrePrepareRound.{JoinConsensus, PrePrepare, StartConsensus}
import com.github.pheymann.scala.bft.model.{ClientRequest, RequestDelivery}
import com.github.pheymann.scala.bft.replica.{Replica, ReplicaContext}
import com.github.pheymann.scala.bft.replica.messaging.MessageBrokerActor.NewConsensusInstance
import com.github.pheymann.scala.bft.util.{LoggingUtil, RequestDigitsGenerator}

import scala.concurrent.{Await, Future}
import scala.util.control.NonFatal

abstract class ConsensusInstance()
                                (implicit
                                  system:         ActorSystem,
                                  replicaContext: ReplicaContext
                                ) extends LoggingUtil {

  import system.dispatcher
  import BftReplicaConfig.consensusTimeout

  protected def runConsensus(request: ClientRequest): Boolean = {
    val requestDigits = RequestDigitsGenerator.generateDigits(request)

    val consensusFut = replicaContext.replicas.retrieveSessionKeys.flatMap { sessionKeys =>
      val requestMacs: Map[Long, Mac] = sessionKeys
        .map { case (id, key) =>
          id -> RequestDigitsGenerator.generateMAC(requestDigits, key)
        }(collection.breakOut)

      implicit val consensusContext = ConsensusContext(
        replicaContext.replicas.self.sequenceNumber,
        replicaContext.replicas.self.view,
        request,
        requestDigits,
        requestMacs
      )

      val instanceRef = system.actorOf(Props(new ConsensusInstanceActor()))

      // forward messages to the current consensus instance
      replicaContext.messaging.messageBrokerRef ! NewConsensusInstance(instanceRef)

      startConsensus(instanceRef)
    }

    try {
      Await.result(consensusFut, BftReplicaConfig.consensusDuration) match {
        case FinishedConsensus => true
        case other =>
          error(s"${logAborted(requestDigits)}: $other")
          false
      }
    }
    catch {
      case NonFatal(cause) =>
        error(cause, logAborted(requestDigits))
        false
    }
  }

  protected def startConsensus(instanceRef: ActorRef): Future[Any]

  private def logAborted(digits: RequestDigits): String = {
    "{%d,%d,[%s]}.aborted".format(
      replicaContext.replicas.self.sequenceNumber,
      replicaContext.replicas.self.view,
      digits.mkString("")
    )
  }

}

case class LeaderConsensus()
                          (implicit system: ActorSystem, replicaContext: ReplicaContext) extends ConsensusInstance {

  import com.github.pheymann.scala.bft.BftReplicaConfig.consensusTimeout

  override protected def startConsensus(instanceRef: ActorRef) = instanceRef ? StartConsensus

  def ? (request: ClientRequest): Boolean = runConsensus(request)

}

case class FollowerConsensus()
                            (implicit system: ActorSystem, replicaContext: ReplicaContext) extends ConsensusInstance {

  import com.github.pheymann.scala.bft.BftReplicaConfig.consensusTimeout

  override protected def startConsensus(instanceRef: ActorRef) = instanceRef ? JoinConsensus

  def ? (message: PrePrepare, requestDelivery: RequestDelivery): Boolean = {
    val isValid = {
      message.sequenceNumber == requestDelivery.sequenceNumber &&
        message.view == requestDelivery.view &&
        replicaContext.storage.hasAcceptedOrUnknown(message)
    }

    if (isValid)
      runConsensus(requestDelivery.request)
    else {
      error(s"request.invalid: ${requestDelivery.toLog},${message.toLog}")
      false
    }
  }

}
