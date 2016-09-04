package com.github.pheymann.scala.bft.replica.messaging

import akka.actor.Props
import com.github.pheymann.scala.bft.consensus.PrepareRound.Prepare
import com.github.pheymann.scala.bft.replica.messaging.MessageBrokerActor.{ConsumeMessage, NewConsensusInstance}
import com.github.pheymann.scala.bft.{BftReplicaSpec, WithActorSystem}

class MessageBrokerActorSpec extends BftReplicaSpec {

  "The MessageBroker" should {
    "buffer messages in a queue if forwarding is deactivated" in new WithActorSystem {
      val brokerRef = system.actorOf(Props(new MessageBrokerActor()))
      val message   = Prepare(0, 0, 0, Array.empty[Byte])

      within(testDuration) {
        brokerRef ! NewConsensusInstance(self)
        brokerRef ! message
        brokerRef ! ConsumeMessage

        expectMsg(message)
        expectNoMsg(noMessageDuration)
      }
    }

    "active forwarding if consumer request a message but non is available" in new WithActorSystem {
      val brokerRef = system.actorOf(Props(new MessageBrokerActor()))
      val message   = Prepare(0, 0, 0, Array.empty[Byte])

      within(testDuration) {
        brokerRef ! NewConsensusInstance(self)
        brokerRef ! ConsumeMessage

        expectNoMsg(noMessageDuration)

        // message is forwarded
        brokerRef ! message
        // message is buffered
        brokerRef ! message

        expectMsg(message)
        expectNoMsg(noMessageDuration)
      }
    }
  }

}
