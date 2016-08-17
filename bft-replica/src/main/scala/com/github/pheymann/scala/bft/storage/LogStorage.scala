package com.github.pheymann.scala.bft.storage

import akka.actor.{ActorSystem, ExtendedActorSystem, Extension, ExtensionId, ExtensionIdProvider}
import com.github.pheymann.scala.bft.consensus.ConsensusMessage
import com.github.pheymann.scala.bft.util.ClientRequest

trait LogStorage extends Extension {

  def startForRequest(request: ClientRequest):  Unit
  def addPrePrepare(message: ConsensusMessage): Unit
  def addPrepare(message: ConsensusMessage):    Unit
  def addCommit(message: ConsensusMessage):     Unit
  def persist(message: ConsensusMessage):       Unit

}

object LogStorage extends ExtensionId[LogStorage]
                  with    ExtensionIdProvider {

  override def lookup = this

  override def createExtension(system: ExtendedActorSystem): LogStorage = new LogStorageInterface()(system)

}

class LogStorageInterface(implicit system: ActorSystem) extends LogStorage {

  //TODO implement storage procedures

  override def startForRequest(request: ClientRequest) {

  }

  override def addPrePrepare(message: ConsensusMessage) {

  }

  override def addPrepare(message: ConsensusMessage) {

  }

  override def addCommit(message: ConsensusMessage) {

  }

  override def persist(message: ConsensusMessage) {

  }

}
