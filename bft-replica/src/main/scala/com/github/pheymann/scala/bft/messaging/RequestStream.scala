package com.github.pheymann.scala.bft.messaging

object RequestStream {

  def generateChunks(delivery: RequestDelivery, chunkSize: Int): Seq[Array[Byte]] = {
    RequestDelivery
      .toBytes(delivery)
      .grouped(chunkSize)
      .toSeq
  }

  def collectChunks(chunk: Array[Byte], state: RequestStreamState): RequestStreamState = {
    state.chunks.enqueue(chunk)
    state.isComplete = state.expectedChunks == state.chunks.length
    state
  }

  def generateRequest(state: RequestStreamState): RequestDelivery = {
    //TODO use mutable builder instead of immutable list concatenation
    RequestDelivery.fromBytes(state.chunks.foldLeft(Array.empty[Byte])(_ ++ _))
  }

}

final case class RequestStreamState(expectedChunks: Int) {

  var isComplete = false

  val chunks = collection.mutable.Queue[Array[Byte]]()

}
