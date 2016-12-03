package com.github.pheymann.scala.bft.consensus

import cats.free.Free
import com.github.pheymann.scala.bft.messaging.PrepareMessage
import com.github.pheymann.scala.bft.storage.StorageAction._
import com.github.pheymann.scala.bft.messaging.MessagingAction._
import com.github.pheymann.scala.bft.replica.{ReplicaContext, ServiceAction}
import ServiceAction._
import MessageValidation._

object PrepareRound {

  def processPrepare(message: PrepareMessage, state: ConsensusState)
                    (implicit context: ReplicaContext): Free[ServiceAction, ConsensusState] = {
    for {
      validation <- validatePrepare(message, state)
      _ <- {
        if (validation._1) {
          store(message).map { _ =>
            if (validation._2.isPrepared)
              broadcastCommit()
            else
              nothing
          }
        }
        else
          nothing
      }
    } yield validation._2
  }

}
