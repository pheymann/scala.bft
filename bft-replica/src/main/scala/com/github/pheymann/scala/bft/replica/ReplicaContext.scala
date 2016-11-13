package com.github.pheymann.scala.bft.replica

import akka.actor.ActorRef
import com.github.pheymann.scala.bft.consensus.ConsensusState
import com.github.pheymann.scala.bft.messaging.ClientRequest

final case class ReplicaContext(
                                isLeader:       Boolean,
                                view:           Int,

                                receiverRef:  ActorRef,
                                senderRef:    ActorRef,

                                var sequenceNumber: Long
                               )(
                                implicit val config: ReplicaConfig
                               ) {

  var requestOpt:   Option[ClientRequest]   = None
  var consensusOpt: Option[ConsensusState]  = None

}
