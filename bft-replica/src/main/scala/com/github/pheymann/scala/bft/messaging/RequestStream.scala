package com.github.pheymann.scala.bft.messaging

import com.github.pheymann.scala.bft.Mac

object RequestStream {

  def generateChunks(delivery: RequestDelivery, chunkSize: Int): Seq[Array[Byte]] = {
    RequestDelivery
      .toBytes(delivery)
      .grouped(chunkSize)
      .toSeq
  }

  def collectChunks(chunk: RequestChunk, state: RequestStreamState): RequestStreamState = {
    state.chunks.enqueue(chunk)
    state.receivedChunks  += 1
    state.isComplete      = state.expectedChunks == state.receivedChunks
    state
  }

}

final case class RequestStreamState(expectedChunks: Int) {

  var isComplete      = false
  var receivedChunks  = 0

  val chunks = collection.mutable.Queue[RequestChunk]()

}

final case class RequestChunk(chunk: Array[Byte], mac: Mac)
