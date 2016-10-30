package com.github.pheymann.scala.bft.consensus

import cats.free.Free
import com.github.pheymann.scala.bft.messaging.PrepareMessage
import com.github.pheymann.scala.bft.replica.ReplicaAction
import com.github.pheymann.scala.bft.storage.StorePrepare

object PrepareRound {

  import com.github.pheymann.scala.bft.replica.ReplicaLifting._

  def processPrepare(message: PrepareMessage, state: ConsensusState): Free[ReplicaAction, ConsensusState] = {
    for {
      validatedState  <- process(ValidatePrepare(message, state))
      _               <- {
        if (validatedState.isPrepared)
          for {
            _ <- process(StorePrepare(message))
            _ <- process(SendCommitMessage(validatedState))
          } yield validatedState
        else
          assign(validatedState)
      }
    } yield validatedState
  }

}
