package com.github.pheymann.scala.bft.consensus

import cats.free.Free
import com.github.pheymann.scala.bft.messaging.{ClientRequest, PrePrepareMessage, RequestDelivery}
import com.github.pheymann.scala.bft.replica.ReplicaAction
import com.github.pheymann.scala.bft.storage.StorePrePrepare

object PrePrepareRound {

  import com.github.pheymann.scala.bft.replica.ReplicaLifting._

  def processLeaderPrePrepare(request: ClientRequest, state: ConsensusState): Free[ReplicaAction, ConsensusState] = {
    for {
      message <- process(SendPrePrepareMessage(state))
      _       <- process(SendClientRequest(request))
      _       <- process(StorePrePrepare(message, request))
      message <- process(SendPrepareMessage(state))
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
            _ <- process(StorePrePrepare(message, delivery.request))
            _ <- process(SendPrepareMessage(state))
          } yield validatedState
        else
          assign(validatedState)
      }
    } yield validatedState
  }

}
