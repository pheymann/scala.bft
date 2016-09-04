package com.github.pheymann.scala.bft.model

case class ClientRequest(
                          clientId:   Long,
                          timestamp:  Long,
                          body:       Array[Byte]
                        ) {

  lazy val toLog: String = {
    s"{$clientId,$timestamp}"
  }

}

object ClientRequest extends ByteArraySerialization[ClientRequest] {

  import ByteArraySerialization._

  override def marshall(request: ClientRequest): Array[Byte] = {
    longToBytes(request.clientId) ++ longToBytes(request.timestamp) ++ request.body
  }

  override def unmarshall(request: Array[Byte]): ClientRequest = {
    ClientRequest(bytesToLong(request)(0), bytesToLong(request)(8), request.splitAt(16)._2)
  }

}
