package com.github.pheymann.scala.bft.storage

import akka.actor.{ActorSystem, ExtendedActorSystem, Extension, ExtensionId, ExtensionIdProvider}
import com.github.pheymann.scala.bft.consensus.ConsensusMessage
import com.github.pheymann.scala.bft.util.ClientRequest

trait LogStorage extends Extension {

  def hasAcceptedOrUnknown(message: ConsensusMessage): Boolean

  def isWithinWatermarks(message: ConsensusMessage): Boolean

  def startForRequest(request: ClientRequest):  Unit
  def addPrePrepare(message: ConsensusMessage): Unit
  def addPrepare(message: ConsensusMessage):    Unit
  def addCommit(message: ConsensusMessage):     Unit
  def finishForRequest(message: ConsensusMessage): Unit

}

object LogStorage extends ExtensionId[LogStorage]
                  with    ExtensionIdProvider {

  override def lookup = this

  override def createExtension(system: ExtendedActorSystem): LogStorage = new LogStorageInterface()(system)

}

class LogStorageInterface(implicit system: ActorSystem) extends LogStorage {

  //TODO implement storage procedures

  override def hasAcceptedOrUnknown(message: ConsensusMessage): Boolean = {
    false
  }

  override def isWithinWatermarks(message: ConsensusMessage): Boolean = {
    false
  }

  override def startForRequest(request: ClientRequest) {

  }

  override def addPrePrepare(message: ConsensusMessage) {

  }

  override def addPrepare(message: ConsensusMessage) {

  }

  override def addCommit(message: ConsensusMessage) {

  }

  override def finishForRequest(message: ConsensusMessage) {

  }

}
