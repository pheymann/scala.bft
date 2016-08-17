package com.github.pheymann.scala.bft.consensus

trait CommitRound { this: Consensus =>

  import CommitRound._

  private var messageCounter = 0

  def commit: Receive = {
    case StartCommit =>
      replicas.sendMessage(Commit(sequenceNumber, view, requestDigits))

    case message@Commit(_sequenceNumber, _view, _requestDigits) =>
      if (isValidMessage(_sequenceNumber, _view, _requestDigits)) {
        if (messageCounter == expectedMessages) {
          storage.store(message)
          //TODO execute request return result
        }
        else
          messageCounter += 1
      }
  }

}

object CommitRound {

  case object StartCommit

  case class Commit(sequenceNumber: Long, view: Long, requestDigits: Array[Byte])

  private final val expectedMessages = 10 //TODO use 2f + 1

}
