package com.github.pheymann.scala.bft.messaging

import com.github.pheymann.scala.bft._

sealed trait ConsensusMessage extends SignableMessage {

  def replicaId:      Int
  def view:           Int
  def sequenceNumber: Long

  lazy val toLog = "{%d,%d,%d}".format(
    replicaId,
    view,
    sequenceNumber
  )

  override def toBytes: Array[Byte] = {
    import com.github.pheymann.scala.bft.util.Serialization._

    intToBytes(replicaId) ++ intToBytes(view) ++ longToBytes(sequenceNumber)
  }

}

final case class PrePrepareMessage(replicaId: Int, view: Int, sequenceNumber: Long) extends ConsensusMessage
final case class PrepareMessage(replicaId: Int, view: Int, sequenceNumber: Long)  extends ConsensusMessage
final case class CommitMessage(replicaId: Int, view: Int, sequenceNumber: Long)   extends ConsensusMessage

final case class SignedConsensusMessage(message: ConsensusMessage, mac: Mac)
final case class SignedRequestChunk(chunk: Array[Byte], mac: Mac)
