package com.github.pheymann.scala.bft.model

case class StartChunkStream(replicaId: Long, numberOfChunks: Int)

case class DataChunk(replicaId: Long, chunk: Array[Byte])
