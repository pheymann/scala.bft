package com.github.pheymann.scala.bft.messaging

import cats.data.Xor
import com.github.pheymann.scala.bft.ScalaBftSpec
import com.github.pheymann.scala.bft.messaging.Receiver.ReceiverContext
import com.github.pheymann.scala.bft.messaging.ReceiverConnection.{ConnectionAlreadyOpenError, ConnectionNotOpenError}

class ReceiverConnectionSpec extends ScalaBftSpec {

  "The ReceiverConnection" should {
    """open a connection (create ReceiverConnectionState) for a sender if no connection
      |exists
    """.stripMargin in {
      val context = new ReceiverContext()

      val result = ReceiverConnection.open(0, 1, testSessionKey, context)

      result.map(_.senderId) should beEqualTo(Xor.right(1))

      context.connections.size should beEqualTo(1)
      context.connections.get(1).map(_.senderId) should beEqualTo(Some(1))

      ReceiverConnection.open(0, 1, testSessionKey, context) should beEqualTo(Xor.left(ConnectionAlreadyOpenError))
    }

    "close a connection if it exists" in {
      val context = new ReceiverContext()

      ReceiverConnection.open(0, 1, testSessionKey, context)
      ReceiverConnection.close(1, context) should beEqualTo(Xor.right(true))

      context.connections.isEmpty should beTrue

      ReceiverConnection.close(1, context) should beEqualTo(Xor.left(ConnectionNotOpenError))
    }
  }

}
