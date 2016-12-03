package com.github.pheymann.scala.bft.consensus

import cats.free.Free
import com.github.pheymann.scala.bft.messaging.CommitMessage
import com.github.pheymann.scala.bft.storage.StorageAction._
import com.github.pheymann.scala.bft.replica.{ReplicaContext, ServiceAction}
import ServiceAction._
import MessageValidation._

object CommitRound {

  def processCommit(message: CommitMessage, state: ConsensusState)
                   (implicit context: ReplicaContext): Free[ServiceAction, ConsensusState] = {
    for {
      validation <- validateCommit(message, state)
      _ <- {
        if (validation._1)
          store(message)
        else
          nothing
      }
    } yield validation._2
  }

}
