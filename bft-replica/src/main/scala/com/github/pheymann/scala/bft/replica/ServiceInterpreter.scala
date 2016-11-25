package com.github.pheymann.scala.bft.replica

import cats.{Id, ~>}
import com.github.pheymann.scala.bft.consensus.{ValidationAction, ValidationInterpreter}
import ServiceAction.EmptyAction
import com.github.pheymann.scala.bft.messaging.{MessagingAction, MessagingInterpreter}

class ServiceInterpreter(validation: ValidationInterpreter, broadcast: MessagingInterpreter)
                        (implicit context: ReplicaContext) extends (ServiceAction ~> Id) {

  override def apply[R](action: ServiceAction[R]): Id[R] = action match {
    case valAction:       ValidationAction[R] => validation(valAction)
    case broadcastAction: MessagingAction[R]  => broadcast(broadcastAction)

    case EmptyAction => ()
  }

}
