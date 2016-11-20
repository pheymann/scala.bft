package com.github.pheymann.scala.bft.messaging

import cats.{Id, ~>}
import com.github.pheymann.scala.bft.messaging.MessagingAction._

class MessagingInterpreter extends (MessagingAction ~> Id) {

  override def apply[R](action: MessagingAction[R]): Id[R] = action match {
    case SendConsensusMsgTo(receiverId, message)    => //TODO
    case SendStreamTo(receiverId, stream)           => //TODO

    case OpenSenderConnection(senderId, sessionKey) => //TODO
    case CloseSenderConnection(senderId)            => //TODO
  }

}
