package com.github.pheymann.scala.bft.messaging

import com.github.pheymann.scala.bft.ScalaBftSpec
import com.github.pheymann.scala.bft.messaging.Sender.SenderContext
import com.github.pheymann.scala.bft.messaging.SenderConnection.SenderSocket

class SenderSpec extends ScalaBftSpec {

  "The Sender" should {
    "broadcast pre-prepare messages to all known receiver (open connections)" in {
      def prePrepare(receiverId: Int) = PrePrepareMessage(0, receiverId, 0, 0L)

      implicit val context = newContext(false, 0, 0)

      checkBroadcast(prePrepare)(Sender.broadcastPrePrepare)
    }

    "broadcast prepare messages to all known receiver (open connections)" in {
      def prepare(receiverId: Int) = PrepareMessage(0, receiverId, 0, 0L)

      implicit val context = newContext(false, 0, 0)

      checkBroadcast(prepare)(Sender.broadcastPrepare)
    }

    "broadcast commit messages to all known receiver (open connections)" in {
      def commit(receiverId: Int) = CommitMessage(0, receiverId, 0, 0L)

      implicit val context = newContext(false, 0, 0)

      checkBroadcast(commit)(Sender.broadcastCommit)
    }

    "broadcast requests to all known host (open connection)" in {
      implicit val context = newContext(false, 0, 0)

      val sentChunks = Seq.newBuilder[ChunkMessage]

      val testSocket = new SenderSocket {
        def send(msg: ScalaBftMessage): Unit = {
          sentChunks += msg.asInstanceOf[ChunkMessage]
        }
      }

      val request   = ClientRequest(0, 0L, Array.empty)
      val delivery  = RequestDelivery(0, 1, 0, 0L, request)

      val sender    = SenderContext()

      SenderConnection.open(1, testSessionKey, testSocket, sender)

      Sender.broadcastRequest(request, sender)

      sentChunks.result().length should beEqualTo {
        RequestStream
          .generateChunks(delivery, context.config.chunkSize)
          .length + 2
      }
    }
  }

  private def checkBroadcast(messagesGen: Int => ConsensusMessage)
                            (broadcast: SenderContext => Unit) = {
    val sentMessages = Seq.newBuilder[ScalaBftMessage]

    val testSocket = new SenderSocket {
      def send(msg: ScalaBftMessage): Unit = {
        sentMessages += msg.asInstanceOf[SignedConsensusMessage].message
      }
    }

    val sender = SenderContext()

    SenderConnection.open(1, testSessionKey, testSocket, sender)
    SenderConnection.open(2, testSessionKey, testSocket, sender)

    broadcast(sender)

    val messages = sentMessages.result()

    messages.length should beEqualTo(2)
    messages.contains(messagesGen(1)) should beTrue
    messages.contains(messagesGen(2)) should beTrue
  }

}
