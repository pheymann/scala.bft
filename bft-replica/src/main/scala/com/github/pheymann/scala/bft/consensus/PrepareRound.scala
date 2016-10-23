package com.github.pheymann.scala.bft.consensus

import cats.free.Free
import com.github.pheymann.scala.bft.messaging.PrepareMessage
import com.github.pheymann.scala.bft.replica.ReplicaAction
import com.github.pheymann.scala.bft.storage.StorePrepare

object PrepareRound {

  import ValidationLifting._
  import com.github.pheymann.scala.bft.replica.ReplicaLifting._
  import com.github.pheymann.scala.bft.storage.StorageLifting._

  def processPrepare(message: PrepareMessage, state: ConsensusState): Free[ReplicaAction, ConsensusState] = {
    for {
      validatedState <- validate(ValidatePrepare(message, state))
      sentState <- {
        if (validatedState.isPrepared)
          for {
            storedState <- store(StorePrepare(message, state))
            sentState   <- process(SendCommitMessage(storedState))
          } yield sentState
        else
          process(Continue(validatedState))
      }
    } yield sentState
  }

}
