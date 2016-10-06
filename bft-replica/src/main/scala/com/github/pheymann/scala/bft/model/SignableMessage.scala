package com.github.pheymann.scala.bft.model

trait SignableMessage {

  def toBytes: Array[Byte]

}
