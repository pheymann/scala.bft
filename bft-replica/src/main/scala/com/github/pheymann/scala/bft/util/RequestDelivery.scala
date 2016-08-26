package com.github.pheymann.scala.bft.util

case class RequestDelivery(
                            sequenceNumber: Long,
                            view:           Long,
                            request:        ClientRequest
                          )

object RequestDelivery extends ByteArraySerialization[RequestDelivery] {

  import ByteArraySerialization._

  override def marshall(request: RequestDelivery): Array[Byte] = {
    longToBytes(request.sequenceNumber) ++ longToBytes(request.view) ++ ClientRequest.marshall(request.request)
  }

  override def unmarshall(request: Array[Byte]): RequestDelivery = {
    RequestDelivery(bytesToLong(request)(0), bytesToLong(request)(8), ClientRequest.unmarshall(request.splitAt(16)._2))
  }

}
