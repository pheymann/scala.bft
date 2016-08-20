package com.github.pheymann.scala.bft.consensus

import akka.actor.ActorSystem
import akka.pattern.ask
import com.github.pheymann.scala.bft.consensus.PrePrepareRound.StartConsensus
import com.github.pheymann.scala.bft.replica.ReplicaContext
import com.github.pheymann.scala.bft.util.ClientRequest

case class LeaderConsensus(request: ClientRequest)
                          (implicit val system: ActorSystem, val replicaContext: ReplicaContext) extends ConsensusInstance(request) {

  import com.github.pheymann.scala.bft.BftReplicaConfig.consensusTimeout

  override def start() = instanceRef ? StartConsensus

}
