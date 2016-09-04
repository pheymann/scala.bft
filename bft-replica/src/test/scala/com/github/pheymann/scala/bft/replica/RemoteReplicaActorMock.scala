package com.github.pheymann.scala.bft.replica

import akka.actor.{Actor, ActorRef}
import com.github.pheymann.scala.bft.consensus.ConsensusMessage
import com.github.pheymann.scala.bft.model.{DataChunk, StartChunkStream}
import com.github.pheymann.scala.bft.util.ActorLoggingUtil

class RemoteReplicaActorMock(specRef: ActorRef) extends Actor with ActorLoggingUtil {

  import RemoteReplicaActorMock._

  override def receive = {
    case _: ConsensusMessage => specRef ! ReceivedConsensusMessage

    case StartChunkStream(replicaId, numOfChunks) =>
      debug(s"mock.received.start-stream: $replicaId,$numOfChunks")
      specRef ! ReceivedStartStream(numOfChunks)

    case DataChunk(_, _) =>
      debug("mock.received.chunk")
      specRef ! ReceivedDataChunk
  }

}

object RemoteReplicaActorMock {

  case object ReceivedConsensusMessage

  case class ReceivedStartStream(numOfChunks: Int)
  case object ReceivedDataChunk

}
