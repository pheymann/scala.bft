package com.github.pheymann.scala.bft.messaging

import com.github.pheymann.scala.bft.ScalaBftSpec

class RequestDeliverySpec extends ScalaBftSpec {

  "The RequestDelivery wrapper class" should {
    "provide functions to marhall and unmarhall an instance to and from a byte array" in {
      val request = RequestDelivery(0, 0, 0L, ClientRequest(0, 0L, Array.empty))
      val array   = RequestDelivery.toBytes(request)

      val reconstructed = RequestDelivery.fromBytes(array)

      reconstructed.replicaId  === request.replicaId
      reconstructed.view       === request.view
      reconstructed.sequenceNumber === request.sequenceNumber
    }
  }

}
