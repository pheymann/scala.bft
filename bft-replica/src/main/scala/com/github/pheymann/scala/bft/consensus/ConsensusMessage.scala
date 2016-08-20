package com.github.pheymann.scala.bft.consensus

trait ConsensusMessage {

  def replicaId:      Long
  def sequenceNumber: Long
  def view:           Long
  def requestDigits:  Array[Byte]

  lazy val toLog = "{%d,%d,[%s]}".format(
    sequenceNumber,
    view,
    requestDigits.mkString("")
  )

}
