package com.github.pheymann.scala.bft.replica.messaging

import akka.actor.{Actor, ActorRef}
import com.github.pheymann.scala.bft.consensus.ConsensusMessage
import com.github.pheymann.scala.bft.util.ActorLoggingUtil

class MessageBrokerActor() extends Actor with ActorLoggingUtil {

  import MessageBrokerActor._

  private val queue = collection.mutable.Queue[Any]()

  private var forwardMessage          = false
  private var consensusRef: ActorRef  = _

  debug("started")

  override def receive = {
    case message: ConsensusMessage =>
      debug(s"message.received: ${message.toLog}")

      if (forwardMessage) {
        forwardMessage = false

        consensusRef ! message
      }
      else
        queue += message

    case ConsumeMessage =>
      if (queue.nonEmpty)
        consensusRef ! queue.dequeue()
      else
        forwardMessage = true

    case NewConsensusInstance(instanceRef) =>
      consensusRef = instanceRef
  }

}

object MessageBrokerActor {

  case object ConsumeMessage

  case class NewConsensusInstance(consensusRef: ActorRef)

}
