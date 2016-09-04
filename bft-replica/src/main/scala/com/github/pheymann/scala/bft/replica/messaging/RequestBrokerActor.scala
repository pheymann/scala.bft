package com.github.pheymann.scala.bft.replica.messaging

import akka.actor.{Actor, ActorRef, Props}
import com.github.pheymann.scala.bft.model.{DataChunk, RequestDelivery, StartChunkStream}
import com.github.pheymann.scala.bft.util.ActorLoggingUtil

class RequestBrokerActor(publisherRef: ActorRef) extends Actor with ActorLoggingUtil {

  private var chunkStreamReceiverRefOpt = Option.empty[ActorRef]

  debug("started")

  override def receive = {
    case StartChunkStream(replicaId, numberOfChunks) =>
      chunkStreamReceiverRefOpt = chunkStreamReceiverRefOpt.fold {
        Some(context.actorOf(Props(new ChunkDataStreamReceiverActor(numberOfChunks, self))))
      } { receiverRef =>
        error(s"request.received.aborted: ${receiverRef.path.name}")
        context.stop(receiverRef)

        Some(context.actorOf(Props(new ChunkDataStreamReceiverActor(numberOfChunks, self)), s"request.chunk.stream.$replicaId"))
      }

    case chunk: DataChunk =>
      chunkStreamReceiverRefOpt match {
        case Some(receiverRef)  => receiverRef ! chunk
        case None               => error(s"request.received.unexpected.chunk: ${chunk.replicaId}")
      }

    case request: RequestDelivery =>
      chunkStreamReceiverRefOpt = chunkStreamReceiverRefOpt.fold {
        log.error(s"request.received.unexpected.request: ${request.toLog}")
        None
      } { receiverRef =>
        log.info(s"request.received: ${request.toLog}")
        context.stop(receiverRef)
        None
      }

      publisherRef ! request
  }

}
