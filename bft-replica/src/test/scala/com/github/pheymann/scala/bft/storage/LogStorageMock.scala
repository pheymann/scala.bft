package com.github.pheymann.scala.bft.storage

import akka.actor.ActorRef
import com.github.pheymann.scala.bft.consensus.CommitRound.Commit
import com.github.pheymann.scala.bft.consensus.ConsensusMessage
import com.github.pheymann.scala.bft.consensus.PrePrepareRound.PrePrepare
import com.github.pheymann.scala.bft.consensus.PrepareRound.Prepare
import com.github.pheymann.scala.bft.util.ClientRequest
import com.github.pheymann.scala.bft.util.StorageMessageCollectorActor._

trait LogStorageMock extends LogStorage {

  def _logCollectorRef: ActorRef

  override def startForRequest(request: ClientRequest) {
    _logCollectorRef ! Start(request)
  }

  override def addPrePrepare(message: ConsensusMessage) {
    _logCollectorRef ! AddPrePrepare(message.asInstanceOf[PrePrepare])
  }

  override def addPrepare(message: ConsensusMessage) {
    _logCollectorRef ! AddPrepare(message.asInstanceOf[Prepare])
  }

  override def addCommit(message: ConsensusMessage) {
    _logCollectorRef ! AddCommit(message.asInstanceOf[Commit])
  }

  override def finishForRequest(message: ConsensusMessage) {
    _logCollectorRef ! Finish(message.asInstanceOf[Commit])
  }

}
