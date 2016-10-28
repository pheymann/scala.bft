package com.github.pheymann.scala.bft.util

import java.nio.ByteBuffer

object Serialization extends LongSerialization with IntSerialization

trait LongSerialization {

  def longToBytes(value: Long): Array[Byte] = {
    ByteBuffer.allocate(8).putLong(value).array()
  }

  def bytesToLong(objArray: Array[Byte], index: Int): Long = {
    ByteBuffer.wrap(objArray).getLong(index)
  }

}

trait IntSerialization {

  def intToBytes(value: Int): Array[Byte] = {
    ByteBuffer.allocate(4).putInt(value).array()
  }

  def bytesToInt(objArray: Array[Byte], index: Int): Int = {
    ByteBuffer.wrap(objArray).getInt(index)
  }

}
