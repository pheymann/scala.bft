package com.github.pheymann.scala.bft.consensus

trait ConsensusRound extends ConsensusActor {

  import ConsensusRound._

  protected def message: ConsensusMessage
  protected def expectedMessages: Int
  protected def executeMessage(message: ConsensusMessage): Unit

  protected var roundHasStarted = false
  protected var roundIsComplete = false
  protected var messageCounter  = 0

  protected def isValidMessage(message: ConsensusMessage): Boolean = {
    //TODO implement is valid
    false
  }

  override def receive = {
    case _: StartRound =>
      roundHasStarted = true

      replicas.sendMessage(message)

      if (roundIsComplete)
        executeMessage(message)

    case message: ConsensusMessage =>
      if (isValidMessage(message)) {
        if (messageCounter == expectedMessages) {
          if (roundHasStarted)
            executeMessage(message)
          else
            roundIsComplete = true
        }
        else
          messageCounter += 1
      }
  }

}

object ConsensusRound {

  trait StartRound

}
