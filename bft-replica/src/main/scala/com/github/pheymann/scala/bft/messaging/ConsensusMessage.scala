package com.github.pheymann.scala.bft.messaging

import com.github.pheymann.scala.bft._

sealed trait ConsensusMessage {

  def replicaId:      Int
  def view:           Int
  def sequenceNumber: Long

  lazy val toLog = "{%d,%d,%d}".format(
    replicaId,
    view,
    sequenceNumber
  )

}

final case class PrepareMessage(replicaId: Int, view: Int, sequenceNumber: Long)  extends ConsensusMessage
final case class CommitMessage(replicaId: Int, view: Int, sequenceNumber: Long)   extends ConsensusMessage

final case class SignedConsensusMessage(message: ConsensusMessage, mac: Mac)
