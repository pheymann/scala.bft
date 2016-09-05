package com.github.pheymann.scala.bft.model

case class RequestDelivery(
                            sequenceNumber: Long,
                            view:           Long,
                            request:        ClientRequest
                          ) {

  lazy val toLog = s"{$sequenceNumber,$view,${request.toLog}}"

}

object RequestDelivery extends ByteArraySerialization[RequestDelivery] {

  import ByteArraySerialization._

  override def marshall(request: RequestDelivery): Array[Byte] = {
    longToBytes(request.sequenceNumber) ++ longToBytes(request.view) ++ ClientRequest.marshall(request.request)
  }

  override def unmarshall(request: Array[Byte]): RequestDelivery = {
    RequestDelivery(bytesToLong(request)(0), bytesToLong(request)(8), ClientRequest.unmarshall(request.splitAt(16)._2))
  }

}
