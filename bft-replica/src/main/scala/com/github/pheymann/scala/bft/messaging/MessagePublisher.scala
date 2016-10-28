package com.github.pheymann.scala.bft.messaging

import akka.stream.actor.ActorPublisher
import akka.stream.actor.ActorPublisherMessage.Request

class MessagePublisher extends ActorPublisher[Any] {

  import MessagePublisher._

  private val queue = collection.mutable.Queue[Any]()

  override def receive = {
    case Publish(message) =>
      queue.enqueue(message)
      publishIfNeeded()

    case Request(_) => publishIfNeeded()
  }

  private def publishIfNeeded(): Unit = {
    while (queue.nonEmpty && isActive && totalDemand > 0) {
      onNext(queue.dequeue())
    }
  }

}

object MessagePublisher {

  final case class Publish(message: Any)

}
