package com.github.pheymann.scala.bft.messaging

import akka.actor.Props
import com.github.pheymann.scala.bft.ScalaBftSpec
import com.github.pheymann.scala.bft.messaging.SenderActor.{CloseSenderConnection, OpenSenderConnection}
import com.github.pheymann.scala.bft.util.AuthenticationGenerator._

import scala.concurrent.duration._

class ReceiverActorSpec extends ScalaBftSpec {

  sequential

//  "The ReceiverActor" should {
//    "verify and buffer incoming messages from known connections" in new WithActorSystem {
//      implicit val context = newContext(false, 0, 0, self)
//
//      import context.config
//
//      val message       = PrePrepareMessage(0, 0, 0, 0L)
//      val signedMessage = SignedConsensusMessage(message, generateMAC(message, testSessionKey))
//
//      within(testDuration) {
//        val receiverRef = system.actorOf(Props(new ReceiverActor()), "spec.receiver")
//
//        receiverRef ! OpenConnection(0, testSessionKey)
//        receiverRef ! signedMessage
//        receiverRef ! Request
//
//        expectMsgPF() {
//          case msg: OpenSenderConnection => msg.receiverId should beEqualTo(0)
//        }
//        expectMsgPF() {
//          case msg: ConnectionSession => msg.sessionKey.length should beEqualTo(16)
//        }
//        expectMsg(message)
//      }
//    }
//
//    "ignore incoming messages from unknown connections" in new WithActorSystem {
//      implicit val context = newContext(false, 0, 0, self)
//
//      import context.config
//
//      val message       = PrePrepareMessage(0, 0, 0, 0L)
//      val signedMessage = SignedConsensusMessage(message, generateMAC(message, testSessionKey))
//
//      within(testDuration) {
//        val receiverRef = system.actorOf(Props(new ReceiverActor()), "spec.receiver")
//
//        receiverRef ! signedMessage
//        receiverRef ! Request
//
//        expectMsg(NoMessage)
//      }
//    }
//
//    "open a connection only if it doesn't exists already" in new WithActorSystem {
//      implicit val context = newContext(false, 0, 0, self)
//
//      import context.config
//
//      val message       = PrePrepareMessage(0, 0, 0, 0L)
//      val signedMessage = SignedConsensusMessage(message, generateMAC(message, testSessionKey))
//
//      within(testDuration) {
//        val receiverRef = system.actorOf(Props(new ReceiverActor()), "spec.receiver")
//
//        receiverRef ! OpenConnection(0, testSessionKey)
//
//        expectMsgPF() {
//          case msg: OpenSenderConnection => msg.receiverId should beEqualTo(0)
//        }
//        expectMsgPF() {
//          case msg: ConnectionSession => msg.sessionKey.length should beEqualTo(16)
//        }
//
//        receiverRef ! OpenConnection(0, testSessionKey)
//
//        expectMsg(ConnectionAlreadyOpen)
//      }
//    }
//
//    "close a connection only if it exists" in new WithActorSystem {
//      implicit val context = newContext(false, 0, 0, self)
//
//      import context.config
//
//      val message       = PrePrepareMessage(0, 0, 0, 0L)
//      val signedMessage = SignedConsensusMessage(message, generateMAC(message, testSessionKey))
//
//      within(testDuration) {
//        val receiverRef = system.actorOf(Props(new ReceiverActor()), "spec.receiver")
//
//        receiverRef ! CloseConnection(0)
//
//        expectNoMsg(500.milliseconds)
//
//        receiverRef ! OpenConnection(0, testSessionKey)
//
//        expectMsgPF() {
//          case msg: OpenSenderConnection => msg.receiverId should beEqualTo(0)
//        }
//        expectMsgPF() {
//          case msg: ConnectionSession => msg.sessionKey.length should beEqualTo(16)
//        }
//
//        receiverRef ! CloseConnection(0)
//
//        expectMsg(CloseSenderConnection(0))
//      }
//    }
//  }

}
