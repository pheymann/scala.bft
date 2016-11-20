package com.github.pheymann.scala.bft.replica

import cats.{Id, ~>}
import com.github.pheymann.scala.bft.consensus.{ValidationAction, ValidationInterpreter}
import com.github.pheymann.scala.bft.messaging.{BroadcastAction, BroadcastInterpreter}
import ServiceAction.EmptyAction

class ServiceInterpreter(validation: ValidationInterpreter, broadcast: BroadcastInterpreter)
                        (implicit context: ReplicaContext) extends (ServiceAction ~> Id) {

  override def apply[R](action: ServiceAction[R]): Id[R] = action match {
    case valAction:       ValidationAction[R] => validation(valAction)
    case broadcastAction: BroadcastAction[R]  => broadcast(broadcastAction)

    case EmptyAction => ()
  }

}
