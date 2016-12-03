package com.github.pheymann.scala.bft.consensus

import cats.free.Free
import com.github.pheymann.scala.bft.messaging.{ClientRequest, PrePrepareMessage, RequestDelivery}
import com.github.pheymann.scala.bft.storage.StorageAction._
import com.github.pheymann.scala.bft.messaging.MessagingAction._
import com.github.pheymann.scala.bft.replica.{ReplicaContext, ServiceAction}
import ServiceAction._
import MessageValidation._

object PrePrepareRound {

  def processLeaderPrePrepare(request: ClientRequest, state: ConsensusState): Free[ServiceAction, ConsensusState] = {
    for {
      _ <- broadcastPrePrepare()
      _ <- broadcastRequest(request)
      _ <- store(request, state)
      _ <- broadcastPrepare()
    } yield {
      state.isPrePrepared = true
      state
    }
  }

  def processFollowerPrePrepare(
                                 message:   PrePrepareMessage,
                                 delivery:  RequestDelivery,
                                 state:     ConsensusState
                               )(implicit context: ReplicaContext): Free[ServiceAction, ConsensusState] = {
    for {
      validation <- validatePrePrepare(message, delivery, state)
      _ <- {
        if (validation._1)
          for {
            _ <- store(delivery.request, state)
            _ <- broadcastPrepare()
          } yield ()
        else
          nothing
      }
    } yield validation._2
  }

}
