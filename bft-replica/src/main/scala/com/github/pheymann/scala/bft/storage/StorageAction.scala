package com.github.pheymann.scala.bft.storage

import cats.free.Free
import com.github.pheymann.scala.bft.consensus.ConsensusState
import com.github.pheymann.scala.bft.messaging.{ClientRequest, CommitMessage, ConsensusMessage}
import com.github.pheymann.scala.bft.replica.ReplicaAction

sealed trait StorageAction[A] extends ReplicaAction[A]

final case class StorePrePrepare(
                                  request: ClientRequest,
                                  message: ConsensusMessage,
                                  state:   ConsensusState
                                )  extends StorageAction[ConsensusState]
final case class StorePrepare(message: ConsensusMessage, state: ConsensusState) extends StorageAction[ConsensusState]
final case class StoreCommit(message: CommitMessage, state: ConsensusState)     extends StorageAction[ConsensusState]

object StorageLifting {

  def store[A](action: StorageAction[A]): Free[ReplicaAction, A] = Free.liftF(action)

}
