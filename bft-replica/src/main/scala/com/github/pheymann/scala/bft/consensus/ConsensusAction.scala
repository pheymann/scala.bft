package com.github.pheymann.scala.bft.consensus

import com.github.pheymann.scala.bft.messaging.ClientRequest
import com.github.pheymann.scala.bft.replica.ReplicaAction

sealed trait ConsensusAction[A] extends ReplicaAction[A]

final case class SendClientRequest(request: ClientRequest) extends ConsensusAction[Unit]

case object SendPrePrepareMessage extends ConsensusAction[Unit]
case object SendPrepareMessage    extends ConsensusAction[Unit]
case object SendCommitMessage     extends ConsensusAction[Unit]
