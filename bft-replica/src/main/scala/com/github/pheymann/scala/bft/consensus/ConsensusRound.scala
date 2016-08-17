package com.github.pheymann.scala.bft.consensus

trait ConsensusRound extends ConsensusRoundActor {

  import ConsensusRound._

  protected def message: ConsensusMessage
  protected def expectedMessages: Int
  protected def executeMessage(message: ConsensusMessage): Unit

  protected def isValidMessage(message: ConsensusMessage): Boolean = {
    //TODO implement is valid
    false
  }

  private var roundHasStarted = false
  private var roundIsComplete = false
  private var messageCounter  = 0

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
