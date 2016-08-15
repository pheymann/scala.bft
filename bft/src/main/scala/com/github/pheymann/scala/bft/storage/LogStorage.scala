package com.github.pheymann.scala.bft.storage

import akka.actor.{ActorSystem, ExtendedActorSystem, Extension, ExtensionId, ExtensionIdProvider}

trait LogStorage extends Extension {

  def store(message: Any): Unit

}

object LogStorage extends ExtensionId[LogStorage]
                  with    ExtensionIdProvider {

  override def lookup = this

  override def createExtension(system: ExtendedActorSystem): LogStorage = new LogStorageInterface()(system)

}

class LogStorageInterface(implicit system: ActorSystem) extends LogStorage {

  override def store(message: Any): Unit = {
    //TODO implement store
  }

}
