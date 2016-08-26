package com.github.pheymann.scala.bft.util

import java.nio.ByteBuffer

trait ByteArraySerialization[T] {

  def marshall(obj: T): Array[Byte]
  def unmarshall(objArray: Array[Byte]): T

}

object ByteArraySerialization {

  def longToBytes(value: Long) = ByteBuffer.allocate(8).putLong(value).array()
  def bytesToLong(objArray: Array[Byte])(index: Int) = ByteBuffer.wrap(objArray).getLong(index)

}
