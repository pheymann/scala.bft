package com.github.pheymann.scala.bft.messaging

import com.github.pheymann.scala.bft.ScalaBftSpec
import com.github.pheymann.scala.bft.messaging.Sender.SenderContext
import com.github.pheymann.scala.bft.messaging.SenderConnection.SenderSocket

class SenderConnectionSpec extends ScalaBftSpec {

  val testSocket = new SenderSocket {
    def send(msg: ScalaBftMessage): Unit = ()
  }

  "The SenderConnection" should {
    "open a connection if it doesn't exists" in {
      val context0 = SenderConnection.open(1, testSessionKey, testSocket, SenderContext())

      context0.connections.size should beEqualTo(1)
      context0.connections(1).sessionKey should beEqualTo(testSessionKey)

      val context1 = SenderConnection.open(1, testSessionKey, testSocket, context0)
      context1.connections.size should beEqualTo(1)
    }

    "close a connection if it exists" in {
      SenderConnection.close(1, SenderContext()).connections.isEmpty should beTrue

      val context = SenderConnection.open(1, testSessionKey, testSocket, SenderContext())

      SenderConnection.close(0, context).connections.size should beEqualTo(1)
      SenderConnection.close(1, context).connections.isEmpty should beTrue
    }
  }

}
