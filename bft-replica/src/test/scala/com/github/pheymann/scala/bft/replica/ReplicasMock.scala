package com.github.pheymann.scala.bft.replica

import akka.actor.ActorRef
import com.github.pheymann.scala.bft.consensus.ConsensusMessage
import com.github.pheymann.scala.bft.util.ClientRequest
import com.github.pheymann.scala.bft.util.RoundMessageCollectorActor.RequestDeliveryMock

class ReplicasMock(val self: Replica, roundCollectorRef: ActorRef) extends Replicas {

  def sendMessage(message: ConsensusMessage) {
    roundCollectorRef ! message
  }
  def sendRequest(message: ConsensusMessage, request: ClientRequest) {
    roundCollectorRef ! RequestDeliveryMock(message, request)
  }

}
