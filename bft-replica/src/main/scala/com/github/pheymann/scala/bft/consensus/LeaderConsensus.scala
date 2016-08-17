package com.github.pheymann.scala.bft.consensus

import com.github.pheymann.scala.bft.consensus.PrePrepareRound.StartConsensus
import com.github.pheymann.scala.bft.util.ClientRequest

case class LeaderConsensus(request: ClientRequest) extends ConsensusInstance {

  prePrepareRound ! StartConsensus

  override def receive = consensusReceive

}
