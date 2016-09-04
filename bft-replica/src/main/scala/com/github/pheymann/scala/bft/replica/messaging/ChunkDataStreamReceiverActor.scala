package com.github.pheymann.scala.bft.replica.messaging

import akka.actor.{Actor, ActorRef}
import com.github.pheymann.scala.bft.model._
import com.github.pheymann.scala.bft.util.ActorLoggingUtil

class ChunkDataStreamReceiverActor(
                                    chunksNumber: Int,
                                    receiver:     ActorRef
                                  ) extends Actor
                                    with    ActorLoggingUtil {

  private var receivedChunks = 0

  private val data = Array.newBuilder[Array[Byte]]

  override def receive = {
    case DataChunk(replicaId, chunk) =>
      data            += chunk
      receivedChunks  += 1

      if (receivedChunks == chunksNumber)
        receiver ! RequestDelivery.unmarshall(data.result.flatten)
  }

}
