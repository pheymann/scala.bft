package com.github.pheymann.scala.bft.replica.messaging

import akka.actor.ActorRef
import com.github.pheymann.scala.bft.BftReplicaConfig
import com.github.pheymann.scala.bft.model._

object ChunkDataStreamSender {

  def send(replicaId: Long, delivery: RequestDelivery, remoteReplicaRefs: Seq[ActorRef]) {
    val chunks = RequestDelivery
      .marshall(delivery)
      .grouped(BftReplicaConfig.messageChunkSize)
      .toSeq

    for (remoteReplicaRef <- remoteReplicaRefs)
      remoteReplicaRef ! StartChunkStream(replicaId, chunks.length)

    for {
      chunk             <- chunks
      remoteReplicaRef  <- remoteReplicaRefs
    } {
      remoteReplicaRef ! DataChunk(replicaId, chunk)
    }
  }

}
