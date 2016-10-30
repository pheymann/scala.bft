package com.github.pheymann.scala.bft.consensus

import com.github.pheymann.scala.bft.messaging.ClientRequest
import com.github.pheymann.scala.bft.replica.ReplicaAction

sealed trait ConsensusAction[A] extends ReplicaAction[A]

final case class SendClientRequest(request: ClientRequest, state: ConsensusState) extends ConsensusAction[Unit]
final case class SendPrePrepareMessage(state: ConsensusState) extends ConsensusAction[Unit]
final case class SendPrepareMessage(state: ConsensusState)  extends ConsensusAction[Unit]
final case class SendCommitMessage(state: ConsensusState)   extends ConsensusAction[Unit]
