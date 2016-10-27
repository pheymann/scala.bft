package com.github.pheymann.scala.bft.consensus

import cats.free.Free
import com.github.pheymann.scala.bft.messaging.ClientRequest
import com.github.pheymann.scala.bft.replica.ReplicaAction
import com.github.pheymann.scala.bft.storage.{StorePrePrepare, StorePrepare}

object PrePrepareRound {

  import com.github.pheymann.scala.bft.storage.StorageLifting._
  import com.github.pheymann.scala.bft.replica.ReplicaLifting._

  def processLeaderPrePrepare(request: ClientRequest, state: ConsensusState): Free[ReplicaAction, ConsensusState] = {
    for {
      message <- process(SendPrePrepareMessage(state))
      _       <- process(SendClientRequest(request))
      _       <- store(StorePrePrepare(message, request))
      message <- process(SendPrepareMessage(state))
      _       <- store(StorePrepare(message))
    } yield updateState(state)
  }

  def processFollowerPrePrepare(state: ConsensusState): Free[ReplicaAction, ConsensusState] = {
    for {
      _ <- process(SendPrepareMessage(state))
    } yield updateState(state)
  }

  private def updateState(state: ConsensusState): ConsensusState = {
    state.isPrePrepared = true
    state.isPrepared = true

    state
  }

}
