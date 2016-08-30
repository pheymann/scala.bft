package com.github.pheymann.scala.bft.model

case class StartChunkStream(chunkNumber: Int)

case class DataChunk(chunk: Array[Byte])
