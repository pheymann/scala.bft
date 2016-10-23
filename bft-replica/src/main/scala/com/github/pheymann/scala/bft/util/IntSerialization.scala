package com.github.pheymann.scala.bft.util

import java.nio.ByteBuffer

object IntSerialization {

  def intToBytes(value: Int): Array[Byte] = {
    ByteBuffer.allocate(4).putInt(value).array()
  }

  def bytesToInt(objArray: Array[Byte], index: Int): Int = {
    ByteBuffer.wrap(objArray).getInt(index)
  }

}
