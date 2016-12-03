package com.github.pheymann.scala.bft.messaging

import com.github.pheymann.scala.bft._

sealed trait ConsensusMessage extends SignableMessage with ScalaBftMessage {

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

final case class SignedConsensusMessage(message: ConsensusMessage, mac: Mac) extends ScalaBftMessage

final case class PrePrepareMessage(
                                    senderId:   Int,
                                    receiverId: Int,
                                    view:       Int,
                                    sequenceNumber: Long
                                  ) extends ConsensusMessage

final case class LeaderPrePrepare(request: ClientRequest) extends ScalaBftMessage
final case class FollowerPrePrepare(message: PrePrepareMessage, delivery: RequestDelivery) extends ScalaBftMessage

final case class PrepareMessage(senderId: Int, receiverId: Int, view: Int, sequenceNumber: Long)  extends ConsensusMessage
final case class CommitMessage(senderId: Int, receiverId: Int, view: Int, sequenceNumber: Long)   extends ConsensusMessage
