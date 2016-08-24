package com.github.pheymann.scala.bft.storage

import akka.actor.ActorRef
import com.github.pheymann.scala.bft.consensus.ConsensusMessage
import com.github.pheymann.scala.bft.util.ClientRequest

class LogStorageMock(
                      specRef: ActorRef,
                      logIsWithWatermarks: Boolean,
                      logHasAcceptedOrUnknown: Boolean
                    ) extends LogStorage {

  import LogStorageMock._

  override def isWithinWatermarks(message: ConsensusMessage)    = logIsWithWatermarks
  override def hasAcceptedOrUnknown(message: ConsensusMessage)  = logHasAcceptedOrUnknown

  override def startForRequest(request: ClientRequest) {
    specRef ! CalledStart
  }

  override def addPrePrepare(message: ConsensusMessage) {
    specRef ! CalledAddPrePrepare
  }

  override def addPrepare(message: ConsensusMessage) {
    specRef ! CalledAddPrepare
  }

  override def addCommit(message: ConsensusMessage) {
    specRef ! CalledAddCommit
  }

  override def finishForRequest(message: ConsensusMessage) {
    specRef ! CalledFinish
  }

}

object LogStorageMock {

  case object CalledStart
  case object CalledAddPrePrepare
  case object CalledAddPrepare
  case object CalledAddCommit
  case object CalledFinish

}
