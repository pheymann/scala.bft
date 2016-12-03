package com.github.pheymann.scala.bft.replica

import cats.{Id, ~>}
import ServiceAction.EmptyAction
import com.github.pheymann.scala.bft.messaging.Sender.SenderContext
import com.github.pheymann.scala.bft.messaging.{MessagingAction, MessagingInterpreter}
import com.github.pheymann.scala.bft.storage.StorageAction

object ServiceInterpreter {

  implicit def interpreter(senderContext: SenderContext, storage: StorageAction ~> Id)
                          (implicit context: ReplicaContext): ServiceAction ~> Id = new (ServiceAction ~> Id) {

    private val messaging = MessagingInterpreter.messaging(senderContext)

    override def apply[R](action: ServiceAction[R]): Id[R] = action match {
      case broadcastAction: MessagingAction[R]  => messaging(broadcastAction)
      case storageAction:   StorageAction[R]    => storage(storageAction)

      case EmptyAction => ()
    }

  }

}
