package com.github.pheymann.scala.bft.consensus

trait ConsensusRound extends ConsensusRoundActor {

  import ConsensusRound._

  protected def message: ConsensusMessage
  protected def expectedMessages: Int
  protected def executeMessage(message: ConsensusMessage): Unit

  protected def isValidMessage(message: ConsensusMessage): Boolean = {
    /*
     * replica has to be in the same view
     */
    message.view == replicas.self.view &&
    /*
     * replica has not accepted a message with sequence number and view
     * and different request digit
     */
    storage.hasAccepted(message) &&
    /*
     * sequence number is between h and H (water marks)
     */
    storage.isWithinWatermarks(message)
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
