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
      validatedState  <- validate(ValidatePrepare(message, state))
      _               <- {
        if (validatedState.isPrepared)
          for {
            _ <- store(StorePrepare(message))
            _ <- process(SendCommitMessage(validatedState))
          } yield validatedState
        else
          process(Continue(validatedState))
      }
    } yield validatedState
  }

}
