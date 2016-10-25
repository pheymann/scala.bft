package com.github.pheymann.scala.bft.messaging

import com.github.pheymann.scala.bft.ScalaBftSpec

class ClientRequestSpec extends ScalaBftSpec {

  "The ClientRequest class" should {
    "provide functions to marshall and unmarshall instance to and from byte arrays" in {
      val request = ClientRequest(0, 0L, Array[Byte](1, 2, 3))
      val array   = ClientRequest.toBytes(request)

      val reconstructed = ClientRequest.fromBytes(array)

      reconstructed.clientId   === request.clientId
      reconstructed.timestamp  === request.timestamp

      reconstructed.body.sameElements(request.body) should beTrue
    }
  }

}
