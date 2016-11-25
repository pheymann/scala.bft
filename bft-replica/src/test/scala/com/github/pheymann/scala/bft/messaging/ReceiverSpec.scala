package com.github.pheymann.scala.bft.messaging

import com.github.pheymann.scala.bft.ScalaBftSpec
import com.github.pheymann.scala.bft.messaging.Receiver.ReceiverContext
import com.github.pheymann.scala.bft.util.AuthenticationGenerator

class ReceiverSpec extends ScalaBftSpec {

  implicit val context = newContext(false, 0, 0)

  import context.config

  "The Receiver" should {
    """add a SignedConsensusMessage to the queue if it is verified, the connection to
      |the sender exists and no stream is active
    """.stripMargin in {
      val message       = CommitMessage(1, 0, 0, 0L)
      val signedMessage = SignedConsensusMessage(message, AuthenticationGenerator.generateMAC(message, testSessionKey))

      val receiver      = new ReceiverContext()

      ReceiverConnection.open(0, 1, testSessionKey, receiver)

      Receiver.addConsensusMessage(signedMessage, receiver)

      receiver.queue.length should beEqualTo(1)
      receiver.queue.dequeue() should beEqualTo(message)
    }

    "not add a SignedConsensusMessage if there is no connection or the mac is invalid" in {
      val message       = CommitMessage(1, 0, 0, 0L)
      val signedMessage = SignedConsensusMessage(message, AuthenticationGenerator.generateMAC(message, (10 until 27).map(_.toByte).toArray))

      val receiver      = new ReceiverContext()

      // no connection
      Receiver.addConsensusMessage(signedMessage, receiver)
      receiver.queue.isEmpty should beTrue

      // invalid mac
      ReceiverConnection.open(0, 1, testSessionKey, receiver)

      Receiver.addConsensusMessage(signedMessage, receiver)
      receiver.queue.isEmpty should beTrue
    }

    "buffer the message if a stream is active and it is verified" in {
      val message       = CommitMessage(1, 0, 0, 0L)
      val signedMessage = SignedConsensusMessage(message, AuthenticationGenerator.generateMAC(message, testSessionKey))

      val receiver      = new ReceiverContext()

      ReceiverConnection.open(0, 1, testSessionKey, receiver)

      Receiver.addChunkMessage(StartChunk(1, 0, 0L), receiver)
      Receiver.addConsensusMessage(signedMessage, receiver)

      receiver.connections.headOption.flatMap(_._2.messageBuffer.headOption) should beEqualTo(Some(message))
      receiver.queue.isEmpty should beTrue
    }
  }

}
