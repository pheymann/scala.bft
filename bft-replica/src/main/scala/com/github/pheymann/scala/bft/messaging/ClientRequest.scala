package com.github.pheymann.scala.bft.messaging

case class ClientRequest(
                          clientId:   Int,
                          timestamp:  Long,
                          body:       Array[Byte]
                        ) extends ScalaBftMessage {

  lazy val toLog = s"{$clientId,$timestamp}"

}

object ClientRequest {

  import com.github.pheymann.scala.bft.util.Serialization._

  def toBytes(request: ClientRequest): Array[Byte] = {
    intToBytes(request.clientId) ++ longToBytes(request.timestamp) ++ request.body
  }

  def fromBytes(array: Array[Byte], offset: Int = 0, limitOpt: Option[Int] = None): ClientRequest = {
    ClientRequest(
      bytesToInt(array, offset),
      bytesToLong(array, offset + 4),
      array.slice(offset + 12, limitOpt.getOrElse(array.length))
    )
  }

}
