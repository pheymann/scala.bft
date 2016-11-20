package com.github.pheymann.scala.bft.consensus

import cats.free.Free
import com.github.pheymann.scala.bft.messaging.{ClientRequest, PrePrepareMessage, RequestDelivery}
import com.github.pheymann.scala.bft.storage.StorageAction._
import ValidationAction._
import com.github.pheymann.scala.bft.messaging.BroadcastAction._
import com.github.pheymann.scala.bft.replica.ServiceAction
import ServiceAction._

object PrePrepareRound {

  def processLeaderPrePrepare(request: ClientRequest, state: ConsensusState): Free[ServiceAction, ConsensusState] = {
    for {
      _ <- broadcastPrePrepare(state)
      _ <- broadcastRequest(request, state)
      _ <- store(request, state)
      _ <- broadcastPrepare(state)
    } yield {
      state.isPrePrepared = true
      state
    }
  }

  def processFollowerPrePrepare(
                                 message:   PrePrepareMessage,
                                 delivery:  RequestDelivery,
                                 state:     ConsensusState
                               ): Free[ServiceAction, ConsensusState] = {
    for {
      validatedState  <- validate(message, delivery, state)
      _               <- {
        if (validatedState.isPrePrepared)
          for {
            _ <- store(delivery.request, state)
            _ <- broadcastPrepare(state)
          } yield ()
        else
          empty
      }
    } yield validatedState
  }

}
