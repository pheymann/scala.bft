package com.github.pheymann.scala.bft.consensus

import com.github.pheymann.scala.bft.messaging._
import com.github.pheymann.scala.bft.replica.ReplicaAction

sealed trait ValidationAction[A] extends ReplicaAction[A]

final case class ValidatePrePrepare(
                                     message:   PrePrepareMessage,
                                     delivery:  RequestDelivery,
                                     state:     ConsensusState
                                   ) extends ValidationAction[ConsensusState]
final case class ValidatePrepare(message: PrepareMessage, state: ConsensusState)  extends ValidationAction[ConsensusState]
final case class ValidateCommit(message: CommitMessage, state: ConsensusState)    extends ValidationAction[ConsensusState]
