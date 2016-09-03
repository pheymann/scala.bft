package com.github.pheymann.scala.bft.consensus

import com.github.pheymann.scala.bft.replica.MessageBrokerActor.ConsumeMessage

trait ConsensusRound extends ConsensusRoundActor {

  import ConsensusRound._

  protected def expectedMessages: Int
  protected def executeMessage(message: ConsensusMessage): Unit

  def isValidMessage(message: ConsensusMessage): Boolean = {
    /*
     * replica has to be in the same view
     */
    message.view == replicas.self.view &&
    /*
     * message digits == request digits
     */
    message.requestDigits.sameElements(consensusContext.requestDigits) &&
    /*
     * sequence number is between h and H (water marks)
     */
    storage.isWithinWatermarks(message)
  }

  private var roundHasStarted = false
  private var roundIsComplete = false
  private var messageCounter  = 1

  override def receive = {
    case _: StartRound =>
      infoQuery("start")
      roundHasStarted = true

      replicas.sendMessage(message)

      if (roundIsComplete) {
        debugQuery("start", "consensus", "reached")
        executeMessage(message)
      }

      sender ! ConsumeMessage

    case message: ConsensusMessage =>
      if (!roundIsComplete && isValidMessage(message)) {
        if (messageCounter == expectedMessages) {
          infoQuery("consensus", "reached")
          messageCounter += 1

          if (roundHasStarted)
            executeMessage(message)
          else
            roundIsComplete = true
        }
        else {
          debugQuery("consensus", s"messages: $messageCounter")
          messageCounter += 1
        }
      }

      sender() ! ConsumeMessage
  }

}

object ConsensusRound {

  trait StartRound

}
