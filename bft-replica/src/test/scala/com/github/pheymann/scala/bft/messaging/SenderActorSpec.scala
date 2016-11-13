package com.github.pheymann.scala.bft.messaging

import akka.actor.Props
import com.github.pheymann.scala.bft.ScalaBftSpec
import com.github.pheymann.scala.bft.messaging.SenderActor.{BroadcastPrePrepare, BroadcastRequest, OpenSenderConnection}

class SenderActorSpec extends ScalaBftSpec {

  "The SenderActor" should {
    "broadcast a consensus message to all known replicas" in new WithActorSystem {
      implicit val context = newContext(false, 0, 0, self)

      within(testDuration) {
        val senderRef = system.actorOf(Props(new SenderActor()), "spec-sender")

        senderRef ! OpenSenderConnection(0, self, testSessionKey)
        senderRef ! OpenSenderConnection(1, self, testSessionKey)
        senderRef ! BroadcastPrePrepare

        expectMsgPF() {
          case signedMsg: SignedConsensusMessage => signedMsg.message.receiverId should beEqualTo(1)
        }
        expectMsgPF() {
          case signedMsg: SignedConsensusMessage => signedMsg.message.receiverId should beEqualTo(0)
        }
      }
    }

    "broadcast a request as stream to all known replicas" in new WithActorSystem {
      implicit val context = newContext(false, 0, 0, self)

      val request   = ClientRequest(0, 0L, Array.empty[Byte])
      val delivery  = RequestDelivery(0, 0, 0, 0L, request)

      within(testDuration) {
        val senderRef = system.actorOf(Props(new SenderActor()), "spec-sender")

        senderRef ! OpenSenderConnection(0, self, testSessionKey)
        senderRef ! OpenSenderConnection(1, self, testSessionKey)
        senderRef ! BroadcastRequest(request)

        def expectedStreamMessage(receiverId: Int) = {
          def expectedSignedChunk = expectMsgPF() {
            case signedMsg: SignedChunkMessage => signedMsg.receiverId should beEqualTo(receiverId)
          }

          expectMsg(StartChunk(0, receiverId, 0L))
          expectedSignedChunk
          expectedSignedChunk
          expectedSignedChunk
          expectedSignedChunk
          expectedSignedChunk
          expectedSignedChunk
          expectedSignedChunk
          expectMsg(EndChunk(0, receiverId, 0L))
        }

        expectedStreamMessage(1)
        expectedStreamMessage(0)
      }
    }
  }

}
