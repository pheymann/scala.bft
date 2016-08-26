package com.github.pheymann.scala.bft.replica

import akka.actor.{Actor, ActorRef}
import com.github.pheymann.scala.bft.consensus.ConsensusMessage
import com.github.pheymann.scala.bft.model.DataChunk

class RemoteReplicaActorMock(specRef: ActorRef) extends Actor {

  import RemoteReplicaActorMock._

  override def receive = {
    case _: ConsensusMessage  => specRef ! ReceivedConsensusMessage
    case DataChunk(index, _)  => specRef ! ReceivedDataChunk(index)
  }

}

object RemoteReplicaActorMock {

  case object ReceivedConsensusMessage

  case class ReceivedDataChunk(index: Int)

}
