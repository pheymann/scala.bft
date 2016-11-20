package com.github.pheymann.scala.bft.consensus

import cats.free.Free
import com.github.pheymann.scala.bft.messaging.PrepareMessage
import ValidationAction._
import com.github.pheymann.scala.bft.storage.StorageAction._
import com.github.pheymann.scala.bft.messaging.BroadcastAction._
import com.github.pheymann.scala.bft.replica.ServiceAction
import ServiceAction._

object PrepareRound {

  def processPrepare(message: PrepareMessage, state: ConsensusState): Free[ServiceAction, ConsensusState] = {
    for {
      validatedState  <- validate(message, state)
      _               <- {
        if (validatedState.isPrepared)
          for {
            _ <- store(message)
            _ <- broadcastCommit(state)
          } yield ()
        else
          empty
      }
    } yield validatedState
  }

}
