package com.github.pheymann.scala.bft.util

import java.nio.ByteBuffer

object LongSerialization {

  def longToBytes(value: Long): Array[Byte] = {
    ByteBuffer.allocate(8).putLong(value).array()
  }

  def bytesToLong(objArray: Array[Byte], index: Int): Long = {
    ByteBuffer.wrap(objArray).getLong(index)
  }

}
