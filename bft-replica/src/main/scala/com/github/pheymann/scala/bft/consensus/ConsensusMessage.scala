package com.github.pheymann.scala.bft.consensus

import com.github.pheymann.scala.bft.Types.{Mac, RequestDigits}

trait ConsensusMessage {

  def replicaId:      Long
  def sequenceNumber: Long
  def view:           Long
  def requestDigits:  RequestDigits

  def requestMac:     Mac

  lazy val toLog = "{%d,%d,[%s]}".format(
    sequenceNumber,
    view,
    requestDigits.mkString("")
  )

  def withMac(requestMac: Mac): ConsensusMessage

}
