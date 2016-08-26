package com.github.pheymann.scala.bft.util

case class ClientRequest(
                          clientId:   Long,
                          timestamp:  Long,
                          body:       Array[Byte]
                        )

object ClientRequest extends ByteArraySerialization[ClientRequest] {

  import ByteArraySerialization._

  override def marshall(request: ClientRequest): Array[Byte] = {
    longToBytes(request.clientId) ++ longToBytes(request.timestamp) ++ request.body
  }

  override def unmarshall(request: Array[Byte]): ClientRequest = {
    ClientRequest(bytesToLong(request)(0), bytesToLong(request)(8), request.splitAt(16)._2)
  }

}
