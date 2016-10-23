package com.github.pheymann.scala.bft.consensus

import com.github.pheymann.scala.bft.replica.ReplicaAction

sealed trait ConsensusAction[A] extends ReplicaAction[A]

final case class SendCommitMessage(state: ConsensusState) extends ConsensusAction[ConsensusState]
final case class Continue(state: ConsensusState)          extends ConsensusAction[ConsensusState]
