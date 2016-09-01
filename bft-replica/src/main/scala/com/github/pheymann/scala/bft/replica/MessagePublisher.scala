package com.github.pheymann.scala.bft.replica

import akka.actor.{ActorRef, Props}
import akka.stream.actor.ActorPublisher
import akka.stream.actor.ActorPublisherMessage.{Cancel, Request}
import com.github.pheymann.scala.bft.consensus.ConsensusMessage
import com.github.pheymann.scala.bft.model.{DataChunk, RequestDelivery, StartChunkStream}
import com.github.pheymann.scala.bft.util.ActorLoggingUtil

class MessagePublisher extends ActorPublisher[Any] with ActorLoggingUtil {

  private val buffer = collection.mutable.Queue[Any]()

  private var chunkStreamReceiverRefOpt = Option.empty[ActorRef]

  override def receive = {
    case message: ConsensusMessage =>
      debug(s"message.received: ${message.toLog}")
      sendNext(message)

    case Request(numberOfMessages) =>
      var counter = 0

      while(counter < numberOfMessages && buffer.nonEmpty) {
        onNext(buffer.dequeue())

        counter += 1
      }

    case chunk: DataChunk =>
      chunkStreamReceiverRefOpt match {
        case Some(receiverRef)  => receiverRef ! chunk
        case None               => error(s"request.received.unexpected.chunk: ${chunk.replicaId}")
      }

    case StartChunkStream(replicaId, numberOfChunks) =>
      chunkStreamReceiverRefOpt = chunkStreamReceiverRefOpt.fold {
        Some(context.actorOf(Props(new ChunkDataStreamReceiverActor(numberOfChunks, self))))
      } { receiverRef =>
        error(s"request.received.aborted: ${receiverRef.path.name}")
        context.stop(receiverRef)

        Some(context.actorOf(Props(new ChunkDataStreamReceiverActor(numberOfChunks, self)), s"request.chunk.stream.$replicaId"))
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

      sendNext(request)

    case Cancel =>
      context.stop(self)
  }

  private def sendNext(message: Any) {
    if (totalDemand > 0) {
      debug("send message directly to stream")
      onNext(message)
    }
    else
      buffer += message
  }

}
