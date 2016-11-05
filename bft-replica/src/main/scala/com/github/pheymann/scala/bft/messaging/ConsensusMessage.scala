package com.github.pheymann.scala.bft.messaging

import com.github.pheymann.scala.bft._

sealed trait ConsensusMessage extends SignableMessage {

  def senderId:       Int
  def receiverId:     Int
  def view:           Int
  def sequenceNumber: Long

  lazy val toLog = "{%d,%d,%d,%d}".format(
    senderId,
    receiverId,
    view,
    sequenceNumber
  )

  override def toBytes: Array[Byte] = {
    import com.github.pheymann.scala.bft.util.Serialization._

    intToBytes(senderId) ++ intToBytes(receiverId) ++ intToBytes(view) ++ longToBytes(sequenceNumber)
  }

}

final case class SignedConsensusMessage(message: ConsensusMessage, mac: Mac)

final case class PrePrepareMessage(
                                    senderId:   Int,
                                    receiverId: Int,
                                    view:       Int,
                                    sequenceNumber: Long
                                  ) extends ConsensusMessage
final case class PrepareMessage(senderId: Int, receiverId: Int, view: Int, sequenceNumber: Long)  extends ConsensusMessage
final case class CommitMessage(senderId: Int, receiverId: Int, view: Int, sequenceNumber: Long)   extends ConsensusMessage

sealed trait ChunkMessage {

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
