package com.github.pheymann.scala.bft.messaging

import akka.actor.Actor
import com.github.pheymann.scala.bft.util.ActorLoggingUtil

class ReceiverActor extends Actor with ActorLoggingUtil {

  private val buffer = collection.mutable.ListBuffer[Any]()

  infoLog("receiver.started")

  override def receive = {
    case message => buffer += message
  }

}
