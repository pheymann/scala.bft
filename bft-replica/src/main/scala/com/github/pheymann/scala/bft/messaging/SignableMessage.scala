package com.github.pheymann.scala.bft.messaging

trait SignableMessage {

  def toBytes: Array[Byte]

}
