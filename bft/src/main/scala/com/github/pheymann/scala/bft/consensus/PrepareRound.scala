package com.github.pheymann.scala.bft.consensus

import com.github.pheymann.scala.bft.consensus.CommitRound.StartCommit

trait PrepareRound { this: Consensus with CommitRound =>

  import PrepareRound._

  private var messageCounter = 0

  def prepare: Receive = {
    case StartPrepare =>
      replicas.sendMessage(Prepare(sequenceNumber, view, requestDigits))

    case message@Prepare(_sequenceNumber, _view, _requestDigits) =>
      if (isValidMessage(_sequenceNumber, _view, _requestDigits)) {
        if (messageCounter == expectedMessages) {
          storage.store(message)
          context.become(commit)
          self ! StartCommit
        }
        else
          messageCounter += 1
      }
  }

}

object PrepareRound {

  case object StartPrepare

  case class Prepare(sequenceNumber: Long, view: Long, requestDigits: Array[Byte])

  private final val expectedMessages = 10 //TODO use 2f

}
