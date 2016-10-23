package com.github.pheymann.scala.bft.consensus

import cats.free.Free
import com.github.pheymann.scala.bft.messaging.CommitMessage
import com.github.pheymann.scala.bft.replica.{ExecuteRequest, ReplicaAction}
import com.github.pheymann.scala.bft.storage.StoreCommit

object CommitRound {

  import ValidationLifting._
  import com.github.pheymann.scala.bft.replica.ReplicaLifting._
  import com.github.pheymann.scala.bft.storage.StorageLifting._

  def processCommit(message: CommitMessage, state: ConsensusState): Free[ReplicaAction, ConsensusState] = {
    for {
      validatedState <- validate(ValidateCommit(message, state))
      processedState <- {
        if (validatedState.isCommited)
          for {
            storedState     <- store(StoreCommit(message, state))
            processedState  <- process(ExecuteRequest(storedState))
          } yield processedState
        else
          process(Continue(validatedState))
      }
    } yield processedState
  }

}
