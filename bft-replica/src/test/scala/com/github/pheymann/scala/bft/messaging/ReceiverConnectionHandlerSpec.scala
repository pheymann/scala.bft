package com.github.pheymann.scala.bft.messaging

import com.github.pheymann.scala.bft.ScalaBftSpec
import com.github.pheymann.scala.bft.messaging.ReceiverConnectionHandler.ReceiverConnectionState
import com.github.pheymann.scala.bft.util.AuthenticationGenerator._

class ReceiverConnectionHandlerSpec extends ScalaBftSpec {

  implicit val testContext = newContext(false, 0, 0)

  import testContext.config

  "The ReceiverConnectionHandler" should {
    "verify incoming ConsensusMessages and return them if no stream is active" in {
      val message       = PrePrepareMessage(0, 0, 0, 0L)
      val signedMessage = SignedConsensusMessage(message, generateMAC(message, testSessionKey))

      val state = ReceiverConnectionState(0, testSessionKey)

      ReceiverConnectionHandler.handleConsensus(signedMessage, state) should beEqualTo(Some(message))
    }

    "verify incoming ConsensusMessages and buffer them if a stream is active" in {
      val message       = PrePrepareMessage(0, 0, 0, 0L)
      val signedMessage = SignedConsensusMessage(message, generateMAC(message, testSessionKey))

      val state = ReceiverConnectionState(0, testSessionKey)

      ReceiverConnectionHandler.handleStreams(StartChunk(0, 0, 0L), state)
      ReceiverConnectionHandler.handleConsensus(signedMessage, state) should beEqualTo(None)
    }

    "verify incoming ConsensusMessages and ignore them if they are invalid" in {
      val message       = PrePrepareMessage(0, 0, 0, 0L)
      val signedMessage = SignedConsensusMessage(message, generateMAC(message, testSessionKey))

      val invalidKey  = Array[Byte](15) ++ testSessionKey.slice(0, 15)
      val state       = ReceiverConnectionState(0, invalidKey)

      ReceiverConnectionHandler.handleConsensus(signedMessage, state) should beEqualTo(None)
    }

    "verify and buffer all messages and chunks until a stream is closed" in {
      val message       = PrePrepareMessage(0, 0, 0, 0L)
      val request       = ClientRequest(0, 0L, Array.empty[Byte])
      val delivery      = RequestDelivery(0, 0, 0, 0L, request)

      val signedMessage = SignedConsensusMessage(message, generateMAC(message, testSessionKey))

      val state = ReceiverConnectionState(0, testSessionKey)

      ReceiverConnectionHandler.handleStreams(StartChunk(0, 0, 0L), state)
      ReceiverConnectionHandler.handleConsensus(signedMessage, state) should beEqualTo(None)

      RequestStream
        .generateChunks(delivery, testContext.config.chunkSize)
        .foreach { chunk =>
          val mac         = generateMAC(generateDigest(chunk), state.sessionKey)
          val signedChunk = SignedRequestChunk(testContext.config.id, delivery.receiverId, testContext.sequenceNumber, chunk, mac)

          ReceiverConnectionHandler.handleStreams(signedChunk, state)
        }

      val resultOpt = ReceiverConnectionHandler.handleStreams(EndChunk(0, 0, 0L), state)

      resultOpt.isDefined     should beTrue
      resultOpt.get.nonEmpty  should beTrue
      resultOpt.get(1)        should beEqualTo(message)
    }

    "verify and buffer all messages and abort request stream if it is invalid" in {
      val message       = PrePrepareMessage(0, 0, 0, 0L)
      val request       = ClientRequest(0, 0L, Array.empty[Byte])
      val delivery      = RequestDelivery(0, 0, 0, 0L, request)

      val signedMessage = SignedConsensusMessage(message, generateMAC(message, testSessionKey))

      val state = ReceiverConnectionState(0, testSessionKey)

      ReceiverConnectionHandler.handleStreams(StartChunk(0, 0, 0L), state)
      ReceiverConnectionHandler.handleConsensus(signedMessage, state) should beEqualTo(None)

      RequestStream
        .generateChunks(delivery, testContext.config.chunkSize)
        .foreach { chunk =>
          // creates invalid macs as no digest is created for chunk
          val mac         = generateMAC(chunk, state.sessionKey)
          val signedChunk = SignedRequestChunk(testContext.config.id, delivery.receiverId, testContext.sequenceNumber, chunk, mac)

          ReceiverConnectionHandler.handleStreams(signedChunk, state)
        }

      ReceiverConnectionHandler.handleStreams(EndChunk(0, 0, 0L), state) should beEqualTo(Some(Seq(message)))
    }
  }

}
