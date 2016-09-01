package com.github.pheymann.scala.bft.replica

import akka.actor.{Actor, ActorRef}
import com.github.pheymann.scala.bft.consensus.ConsensusMessage
import com.github.pheymann.scala.bft.model.{DataChunk, StartChunkStream}

class RemoteReplicaActorMock(specRef: ActorRef) extends Actor {

  import RemoteReplicaActorMock._

  override def receive = {
    case _: ConsensusMessage  => specRef ! ReceivedConsensusMessage
    case StartChunkStream(replicaId, numOfChunks) => specRef ! ReceivedStartStream(numOfChunks)
    case DataChunk(_, _)  => specRef ! ReceivedDataChunk
  }

}

object RemoteReplicaActorMock {

  case object ReceivedConsensusMessage

  case class ReceivedStartStream(numOfChunks: Int)
  case object ReceivedDataChunk

}
