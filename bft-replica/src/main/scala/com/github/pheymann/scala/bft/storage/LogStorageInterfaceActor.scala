package com.github.pheymann.scala.bft.storage

import akka.pattern.ask
import akka.actor.{Actor, ActorRef}
import com.github.pheymann.scala.bft.consensus.ConsensusMessage
import com.github.pheymann.scala.bft.model.ClientRequest
import com.github.pheymann.scala.bft.replica.ReplicaContext
import com.github.pheymann.scala.bft.storage.LogStorageInterfaceActor.LogStorageMessage
import com.github.pheymann.scala.bft.util.ActorLoggingUtil

import scala.concurrent.Await


class LogStorageInterfaceActor(logStorageRef: ActorRef) extends Actor with ActorLoggingUtil {

  override def receive = {
    case message: LogStorageMessage => logStorageRef.tell(message, sender())
  }

}

object LogStorageInterfaceActor {

  private[storage] sealed trait LogStorageMessage

  case class IsAcceptedOrUnknown(message: ConsensusMessage) extends LogStorageMessage
  case class IsWithinWatermarks(message: ConsensusMessage)  extends LogStorageMessage
  case class StartForRequest(request: ClientRequest)      extends LogStorageMessage
  case class AddPrePrepare(message: ConsensusMessage)     extends LogStorageMessage
  case class AddPrepare(message: ConsensusMessage)        extends LogStorageMessage
  case class AddCommit(message: ConsensusMessage)         extends LogStorageMessage
  case class FinishForRequest(message: ConsensusMessage)  extends LogStorageMessage

  def hasAcceptedOrUnknown(message: ConsensusMessage)
                          (implicit replicaContext: ReplicaContext): Boolean = {
    requestLogStorage(IsAcceptedOrUnknown(message))
  }

  def isWithinWatermarks(message: ConsensusMessage)
                        (implicit replicaContext: ReplicaContext): Boolean = {
    requestLogStorage(IsWithinWatermarks(message))
  }

  private def requestLogStorage[R](message: LogStorageMessage)
                                  (implicit replicaContext: ReplicaContext): R = {
    val config = replicaContext.config

    import config.replicaConfig.storageTimeout

    Await.result(replicaContext.storageRef ? message, replicaContext.config.replicaConfig.storageDuration).asInstanceOf[R]
  }

  val name = "scala-bft_log-storage-interface"

}
