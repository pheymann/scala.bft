package com.github.pheymann.scala.bft.messaging

import cats.{Id, ~>}
import com.github.pheymann.scala.bft.messaging.MessagingAction._
import com.github.pheymann.scala.bft.messaging.Sender.SenderContext
import com.github.pheymann.scala.bft.replica.ReplicaContext

object MessagingInterpreter {

  def messaging(senderContext: SenderContext)
               (implicit context: ReplicaContext) = new (MessagingAction ~> Id) {

    override def apply[R](action: MessagingAction[R]): Id[R] = action match {
      case BroadcastRequest(request) => Sender.broadcastRequest(request, senderContext)
      case BroadcastPrePrepare       => Sender.broadcastPrePrepare(senderContext)
      case BroadcastPrepare          => Sender.broadcastPrepare(senderContext)
      case BroadcastCommit           => Sender.broadcastCommit(senderContext)
    }

  }

}
