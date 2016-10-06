package com.github.pheymann.scala.bft.consensus

import com.github.pheymann.scala.bft.model.{ByteArraySerialization, SignableMessage}

trait ConsensusMessage extends SignableMessage {

  import ByteArraySerialization._

  def replicaId:      Long
  def sequenceNumber: Long
  def view:           Long

  lazy val toLog = "{%d,%d,%d}[%s]".format(
    replicaId,
    sequenceNumber,
    view,
    this.getClass.getSimpleName
  )

  override def toBytes: Array[Byte] = longToBytes(replicaId) ++ longToBytes(sequenceNumber) ++ longToBytes(view)

}
