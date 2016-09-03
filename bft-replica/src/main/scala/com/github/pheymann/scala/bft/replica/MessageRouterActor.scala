package com.github.pheymann.scala.bft.replica

import akka.actor.{Actor, ActorRef}
import com.github.pheymann.scala.bft.consensus.ConsensusMessage
import com.github.pheymann.scala.bft.model.{DataChunk, StartChunkStream}
import com.github.pheymann.scala.bft.util.ActorLoggingUtil

class MessageRouterActor(
                          messageBrokerRef: ActorRef,
                          requestBrokerRef: ActorRef
                        ) extends Actor with ActorLoggingUtil {

  override def receive = {
    case message: ConsensusMessage =>
      messageBrokerRef ! message

    case requestMessage@(_: DataChunk | _: StartChunkStream) =>
      requestBrokerRef ! requestMessage
  }

}
