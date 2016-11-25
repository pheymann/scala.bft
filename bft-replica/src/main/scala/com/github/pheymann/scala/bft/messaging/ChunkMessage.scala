package com.github.pheymann.scala.bft.messaging

import com.github.pheymann.scala.bft._

sealed trait ChunkMessage extends ScalaBftMessage {

  def senderId:   Int
  def receiverId: Int
  def sequenceNumber: Long

}

sealed trait SignedChunkMessage extends ChunkMessage {

  def mac: Mac

}

final case class SignedRequestChunk(
                                     senderId:        Int,
                                     receiverId:      Int,
                                     sequenceNumber:  Long,
                                     chunk:           Array[Byte],
                                     mac:             Mac
                                   ) extends SignedChunkMessage

final case class StartChunk(senderId: Int, receiverId: Int, sequenceNumber:  Long) extends ChunkMessage
final case class EndChunk(senderId: Int, receiverId: Int, sequenceNumber:  Long) extends ChunkMessage
