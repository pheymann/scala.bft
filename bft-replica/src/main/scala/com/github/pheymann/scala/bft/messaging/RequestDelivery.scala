package com.github.pheymann.scala.bft.messaging

case class RequestDelivery(replicaId: Int, view: Int, sequenceNumber: Long, request: ClientRequest)

object RequestDelivery {

  import com.github.pheymann.scala.bft.util.Serialization._

  def toBytes(request: RequestDelivery): Array[Byte] = {
    intToBytes(request.replicaId) ++
      intToBytes(request.view) ++
      longToBytes(request.sequenceNumber) ++
      ClientRequest.toBytes(request.request)
  }

  def fromBytes(array: Array[Byte]): RequestDelivery = {
    RequestDelivery(
      bytesToInt(array, 0),
      bytesToInt(array, 4),
      bytesToLong(array, 8),
      ClientRequest.fromBytes(array, 16)
    )
  }

}
