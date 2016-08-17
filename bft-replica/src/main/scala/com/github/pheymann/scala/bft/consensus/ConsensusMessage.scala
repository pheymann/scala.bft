package com.github.pheymann.scala.bft.consensus

trait ConsensusMessage {

  def sequenceNumber: Long
  def view:           Long
  def requestDigits:  Array[Byte]

}
