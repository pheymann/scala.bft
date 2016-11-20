package com.github.pheymann.scala.bft.consensus

import cats.free.Free
import com.github.pheymann.scala.bft.messaging.CommitMessage
import ValidationAction._
import com.github.pheymann.scala.bft.storage.StorageAction._
import com.github.pheymann.scala.bft.replica.ServiceAction
import ServiceAction._

object CommitRound {

  def processCommit(message: CommitMessage, state: ConsensusState): Free[ServiceAction, ConsensusState] = {
    for {
      validatedState  <- validate(message, state)
      _               <- {
        if (validatedState.isCommited)
          store(message)
        else
          empty
      }
    } yield validatedState
  }

}
