package com.github.pheymann.scala.bft.consensus

import com.github.pheymann.scala.bft.consensus.PrePrepareRound.StartConsensus
import com.github.pheymann.scala.bft.replica.ReplicaContext
import com.github.pheymann.scala.bft.util.ClientRequest

case class LeaderConsensus(request: ClientRequest)
                          (implicit val replicaContext: ReplicaContext) extends ConsensusInstance {

  prePrepareRound ! StartConsensus

  override def receive = consensusReceive

}
