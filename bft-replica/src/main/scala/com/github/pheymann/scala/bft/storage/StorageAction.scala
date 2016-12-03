package com.github.pheymann.scala.bft.storage

import cats.free.Free
import com.github.pheymann.scala.bft.consensus.ConsensusState
import com.github.pheymann.scala.bft.messaging._
import com.github.pheymann.scala.bft.replica.ServiceAction

sealed trait StorageAction[R] extends ServiceAction[R]

object StorageAction {

  case object GetLastView extends StorageAction[Int]

  def getLastView: Free[ServiceAction, Int] = GetLastView.liftM

  case object GetLastSequenceNumber extends StorageAction[Long]

  def getLastSequenceNumber: Free[ServiceAction, Long] = GetLastSequenceNumber.liftM

  final case class IsPrePrepareStored(message: PrePrepareMessage) extends StorageAction[Boolean]

  def isPrePrepareStored(message: PrePrepareMessage): Free[ServiceAction, Boolean] = {
    IsPrePrepareStored(message).liftM
  }

  final case class IsPrepareStored(message: PrepareMessage) extends StorageAction[Boolean]

  def isPrepareStored(message: PrepareMessage): Free[ServiceAction, Boolean] = {
    IsPrepareStored(message).liftM
  }

  final case class IsCommitStored(message: CommitMessage) extends StorageAction[Boolean]

  def isCommitStored(message: CommitMessage): Free[ServiceAction, Boolean] = {
    IsCommitStored(message).liftM
  }

  final case class StorePrePrepare(
                                    request:  ClientRequest,
                                    state:    ConsensusState
                                  )  extends StorageAction[Unit]

  def store(request: ClientRequest, state: ConsensusState): Free[ServiceAction, Unit] = {
    StorePrePrepare(request, state).liftM
  }

  final case class StorePrepare(message: PrepareMessage) extends StorageAction[Unit]

  def store(message: PrepareMessage): Free[ServiceAction, Unit] = StorePrepare(message).liftM

  final case class StoreCommit(message: CommitMessage) extends StorageAction[Unit]

  def store(message: CommitMessage): Free[ServiceAction, Unit] = StoreCommit(message).liftM

}
