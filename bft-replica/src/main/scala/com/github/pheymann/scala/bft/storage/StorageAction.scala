package com.github.pheymann.scala.bft.storage

import cats.free.Free
import com.github.pheymann.scala.bft.consensus.ConsensusState
import com.github.pheymann.scala.bft.messaging._
import com.github.pheymann.scala.bft.replica.ReplicaAction

sealed trait StorageAction[A] extends ReplicaAction[A]

final case class StorePrePrepare(
                                  message: PrePrepareMessage,
                                  request: ClientRequest
                                )  extends StorageAction[Unit]
final case class StorePrepare(message: PrepareMessage) extends StorageAction[Unit]
final case class StoreCommit(message: CommitMessage)   extends StorageAction[Unit]

object StorageLifting {

  def store[A](action: StorageAction[A]): Free[ReplicaAction, A] = Free.liftF(action)

}
