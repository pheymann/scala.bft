package com.github.pheymann.scala.bft.storage

import com.github.pheymann.scala.bft.consensus.ConsensusState
import com.github.pheymann.scala.bft.messaging._
import com.github.pheymann.scala.bft.replica.ReplicaAction

sealed trait StorageAction[A] extends ReplicaAction[A]

case object GetLastView           extends StorageAction[Int]
case object GetLastSequenceNumber extends StorageAction[Long]

final case class StorePrePrepare(
                                  request:  ClientRequest,
                                  state:    ConsensusState
                                )  extends StorageAction[Unit]
final case class StorePrepare(message: PrepareMessage) extends StorageAction[Unit]
final case class StoreCommit(message: CommitMessage)   extends StorageAction[Unit]
