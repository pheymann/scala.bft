package com.github.pheymann.scala.bft.consensus

import cats.free.Free
import com.github.pheymann.scala.bft.messaging.{ClientRequest, PrePrepareMessage, RequestDelivery}
import com.github.pheymann.scala.bft.replica.ReplicaAction
import com.github.pheymann.scala.bft.storage.StorePrePrepare

object PrePrepareRound {

  import com.github.pheymann.scala.bft.replica.ReplicaLifting._

  def processLeaderPrePrepare(request: ClientRequest, state: ConsensusState): Free[ReplicaAction, ConsensusState] = {
    for {
      _ <- process(SendPrePrepareMessage)
      _ <- process(SendClientRequest(request))
      _ <- process(StorePrePrepare(request, state))
      _ <- process(SendPrepareMessage)
    } yield {
      state.isPrePrepared = true
      state
    }
  }

  def processFollowerPrePrepare(
                                 message:   PrePrepareMessage,
                                 delivery:  RequestDelivery,
                                 state:     ConsensusState
                               ): Free[ReplicaAction, ConsensusState] = {
    for {
      validatedState  <- process(ValidatePrePrepare(message, delivery, state))
      _               <- {
        if (validatedState.isPrePrepared)
          for {
            _ <- process(StorePrePrepare(delivery.request, state))
            _ <- process(SendPrepareMessage)
          } yield validatedState
        else
          assign(validatedState)
      }
    } yield validatedState
  }

}
