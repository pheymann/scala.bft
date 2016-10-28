package com.github.pheymann.scala.bft.consensus

import cats.free.Free
import com.github.pheymann.scala.bft.messaging.CommitMessage
import com.github.pheymann.scala.bft.replica.{ExecuteRequest, ReplicaAction}
import com.github.pheymann.scala.bft.storage.StoreCommit

object CommitRound {

  import com.github.pheymann.scala.bft.replica.ReplicaLifting._

  def processCommit(message: CommitMessage, state: ConsensusState): Free[ReplicaAction, ConsensusState] = {
    for {
      validatedState <- process(ValidateCommit(message, state))
      processedState <- {
        if (validatedState.isCommited)
          for {
            _ <- process(StoreCommit(message))
            responseState <- process(ExecuteRequest(validatedState))
          } yield responseState
        else
          assign(validatedState)
      }
    } yield processedState
  }

}
