package com.github.pheymann.scala.bft.messaging

import cats.data.Xor
import com.github.pheymann.scala.bft.messaging.MessageSender.NoSessionKey
import com.github.pheymann.scala.bft.util.AuthenticationGenerator
import com.github.pheymann.scala.bft.{Mac, ScalaBftSpec}

class MessageSenderSpec extends ScalaBftSpec {

  "The MessageSender" should {
    "sign consensus messages and send them to the target replica/client if a session key exists" in {
      val message = CommitMessage(0, 0, 0L)

      implicit val config = newConfig(0, 0, 0)

      var digestOpt = Option.empty[Mac]

      MessageSender.sendConsensusMessage(message, (signedMsg, _) => {
        digestOpt = Some(signedMsg.asInstanceOf[SignedConsensusMessage].mac)
      }) should beEqualTo(Xor.right(true))

      digestOpt.isDefined should beTrue
      digestOpt.get should beEqualTo(AuthenticationGenerator.generateMAC(message, config.localSessionKeys.head._2))
    }

    "reject messages which target a replica without a session" in {
      val invalidMessage = CommitMessage(1, 0, 0L)

      implicit val config = newConfig(0, 0, 0)

      var isSent = false

      MessageSender.sendConsensusMessage(invalidMessage, (_, _) => isSent = true) should beEqualTo(Xor.left(NoSessionKey(1)))

      isSent should beFalse
    }

    """brake a client request (RequestDelivery) into chunks, sign and send them to the target replica if
      |a session key exists
    """.stripMargin in {
      val request   = ClientRequest(0, 0L, Array[Byte](1, 2, 3))
      val delivery  = RequestDelivery(0, 0, 0L, request)

      implicit val config = newConfig(0, 0, 0)

      var sentChunks = 0

      MessageSender.sendClientRequest(delivery, (signedChunk, _) => sentChunks += 1) should beEqualTo(Xor.right(true))

      sentChunks should beEqualTo(RequestDelivery.toBytes(delivery).length / testChunkSize + 1)
    }

    "reject a request if no session exists with the target replica" in {
      val request         = ClientRequest(0, 0L, Array[Byte](1, 2, 3))
      val invalidDelivery = RequestDelivery(1, 0, 0L, request)

      implicit val config = newConfig(0, 0, 0)

      var isSent = false

      MessageSender.sendClientRequest(invalidDelivery, (_, _) => isSent = true) should beEqualTo(Xor.left(NoSessionKey(1)))

      isSent should beFalse
    }
  }

}
