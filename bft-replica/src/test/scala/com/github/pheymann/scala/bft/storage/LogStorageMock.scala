package com.github.pheymann.scala.bft.storage

import akka.actor.{ActorSystem, Props}
import com.github.pheymann.scala.bft.consensus.CommitRound.Commit
import com.github.pheymann.scala.bft.consensus.ConsensusMessage
import com.github.pheymann.scala.bft.consensus.PrePrepareRound.PrePrepare
import com.github.pheymann.scala.bft.consensus.PrepareRound.Prepare
import com.github.pheymann.scala.bft.util.{ClientRequest, StorageMessageCollectorActor}
import com.github.pheymann.scala.bft.util.StorageMessageCollectorActor._

trait LogStorageMock extends LogStorage {

  implicit def _system: ActorSystem

  implicit val logCollectorRef = _system.actorOf(Props(new StorageMessageCollectorActor()))

  override def startForRequest(request: ClientRequest) {
    logCollectorRef ! Start(request)
  }

  override def addPrePrepare(message: ConsensusMessage) {
    logCollectorRef ! AddPrePrepare(message.asInstanceOf[PrePrepare])
  }

  override def addPrepare(message: ConsensusMessage) {
    logCollectorRef ! AddPrepare(message.asInstanceOf[Prepare])
  }

  override def addCommit(message: ConsensusMessage) {
    logCollectorRef ! AddCommit(message.asInstanceOf[Commit])
  }

  override def finishForRequest(message: ConsensusMessage) {
    logCollectorRef ! Finish(message.asInstanceOf[Commit])
  }

}
