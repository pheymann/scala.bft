package com.github.pheymann.scala.bft.replica

import akka.actor.ActorRef
import com.github.pheymann.scala.bft.consensus.ConsensusMessage
import com.github.pheymann.scala.bft.model.ClientRequest

class ReplicasMock(specRef: ActorRef, val self: Replica) extends Replicas {

  import ReplicasMock._

  private[replica] val remoteReplicaRefs = Seq.empty

  override def sendMessage(message: ConsensusMessage) {
    specRef ! CalledSendMessage
  }

  override def sendRequest(request: ClientRequest) {
    specRef ! CalledSendRequest
  }

}

object ReplicasMock {

  case object CalledSendMessage
  case object CalledSendRequest

}
