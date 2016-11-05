package com.github.pheymann.scala.bft.messaging

import cats.data.Xor

import scala.util.control.NonFatal

object RequestStream {

  final case class RequestStreamState(sequenceNumber: Long) {

    val chunks = collection.mutable.Queue[Array[Byte]]()

  }

  def generateChunks(delivery: RequestDelivery, chunkSize: Int): Seq[Array[Byte]] = {
    RequestDelivery
      .toBytes(delivery)
      .grouped(chunkSize)
      .toSeq
  }

  def collectChunks(chunk: Array[Byte], state: RequestStreamState): RequestStreamState = {
    state.chunks.enqueue(chunk)
    state
  }

  def generateRequest(state: RequestStreamState): Xor[Throwable, RequestDelivery] = {
    try {
      val builder = Seq.newBuilder[Byte]

      state.chunks.foreach { chunk =>
        builder ++= chunk
      }

      Xor.right(RequestDelivery.fromBytes(builder.result().toArray))
    }
    catch {
      case NonFatal(cause) => Xor.left(cause)

    }
  }

}
