package com.github.pheymann.scala.bft.consensus

import com.github.pheymann.scala.bft.messaging.{ClientRequest, CommitMessage, PrePrepareMessage, PrepareMessage}
import com.github.pheymann.scala.bft.replica.ReplicaAction

sealed trait ConsensusAction[A] extends ReplicaAction[A]

final case class SendClientRequest(request: ClientRequest) extends ConsensusAction[Unit]
final case class SendPrePrepareMessage(state: ConsensusState) extends ConsensusAction[PrePrepareMessage]
final case class SendPrepareMessage(state: ConsensusState)  extends ConsensusAction[PrepareMessage]
final case class SendCommitMessage(state: ConsensusState)   extends ConsensusAction[Unit]

final case class Continue(state: ConsensusState)            extends ConsensusAction[ConsensusState]
