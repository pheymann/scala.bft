package com.github.pheymann.scala.bft.messaging

import akka.actor.{Actor, ActorRef}
import com.github.pheymann.scala.bft.util.ActorLoggingUtil

class ReceiverActor(publisherRef: ActorRef) extends Actor with ActorLoggingUtil {

  infoLog("receiver.started")

  override def receive = {
    case message => publisherRef ! message
  }

}

object ReceiverActor {

  val name = "receiver_actor"

}
