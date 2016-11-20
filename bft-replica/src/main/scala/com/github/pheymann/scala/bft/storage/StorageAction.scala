package com.github.pheymann.scala.bft.storage

import cats.free.Free
import com.github.pheymann.scala.bft.consensus.ConsensusState
import com.github.pheymann.scala.bft.messaging._
import com.github.pheymann.scala.bft.replica.ServiceAction

sealed trait StorageAction[R] extends ServiceAction[R]

object StorageAction {

  case object GetLastView extends StorageAction[Int]

  def getLastView: Free[StorageAction, Int] = Free.liftF(GetLastView)

  case object GetLastSequenceNumber extends StorageAction[Long]

  def getLastSequenceNumber: Free[ServiceAction, Long] = {
    Free.liftF(GetLastSequenceNumber)
  }

  final case class StorePrePrepare(
                                    request:  ClientRequest,
                                    state:    ConsensusState
                                  )  extends StorageAction[Unit]

  def store(request: ClientRequest, state: ConsensusState): Free[ServiceAction, Unit] = {
    Free.liftF(StorePrePrepare(request, state))
  }

  final case class StorePrepare(message: PrepareMessage) extends StorageAction[Unit]

  def store(message: PrepareMessage): Free[ServiceAction, Unit] = {
    Free.liftF(StorePrepare(message))
  }

  final case class StoreCommit(message: CommitMessage) extends StorageAction[Unit]

  def store(message: CommitMessage): Free[ServiceAction, Unit] = {
    Free.liftF(StoreCommit(message))
  }

}
