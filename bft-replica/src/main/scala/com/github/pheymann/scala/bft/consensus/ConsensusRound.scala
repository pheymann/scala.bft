package com.github.pheymann.scala.bft.consensus

import com.github.pheymann.scala.bft.replica.messaging.MessageBrokerActor.ConsumeMessage
import com.github.pheymann.scala.bft.storage.LogStorageInterfaceActor

trait ConsensusRound extends ConsensusRoundActor {

  import ConsensusRound._

  protected def expectedMessages: Int
  protected def executeMessage(message: ConsensusMessage): Unit

  def isValidMessage(message: ConsensusMessage): Boolean = {
    /*
     * replica has to be in the same view
     */
    message.view == replicaContext.replicas.self.view &&
    /*
     * sequence number is between h and H (water marks)
     */
    LogStorageInterfaceActor.isWithinWatermarks(message)
  }

  private var roundHasStarted = false
  private var roundIsCompleted = false
  private var messageCounter  = 1

  override def receive = {
    case _: StartRound =>
      infoQuery("start")
      roundHasStarted = true

      replicaContext.replicas.sendMessage(message)

      if (roundIsCompleted) {
        debugQuery("start", "consensus", "reached")
        executeMessage(message)
      }

      sender() ! ConsumeMessage

    case message: ConsensusMessage =>
      if (!roundIsCompleted && isValidMessage(message)) {
        if (messageCounter == expectedMessages) {
          infoQuery("consensus", "reached")
          messageCounter += 1

          if (roundHasStarted)
            executeMessage(message)
          else
            roundIsCompleted = true
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
