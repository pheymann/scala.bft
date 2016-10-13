package com.github.pheymann.scala.bft.replica.messaging

import akka.actor.ActorRef
import com.github.pheymann.scala.bft.model._

object ChunkDataStreamSender {

  def send(replicaId: Long, delivery: RequestDelivery, remoteReplicaRefs: Seq[ActorRef])
          (chunkSize: Int): Unit = {
    val chunks = generateChunks(delivery)(chunkSize)

    for (remoteReplicaRef <- remoteReplicaRefs)
      remoteReplicaRef ! StartChunkStream(replicaId, chunks.length)

    for {
      chunk             <- chunks
      remoteReplicaRef  <- remoteReplicaRefs
    } {
      remoteReplicaRef ! DataChunk(replicaId, chunk)
    }
  }

  private[messaging] def generateChunks(delivery: RequestDelivery)
                                       (chunkSize: Int): Seq[Array[Byte]] = {
    RequestDelivery
      .marshall(delivery)
      .grouped(chunkSize)
      .toSeq
  }

}
