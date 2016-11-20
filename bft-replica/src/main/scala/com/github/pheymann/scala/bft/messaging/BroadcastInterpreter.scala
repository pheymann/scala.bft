package com.github.pheymann.scala.bft.messaging

import cats.{Id, ~>}
import com.github.pheymann.scala.bft.messaging.BroadcastAction._

class BroadcastInterpreter extends (BroadcastAction ~> Id) {

  override def apply[R](action: BroadcastAction[R]): Id[R] = action match {
    case BroadcastRequest(request, state) => //TODO
    case BroadcastPrePrepare(state)       => //TODO
    case BroadcastPrepare(state)          => //TODO
    case BroadcastCommit(state)           => //TODO
  }

}
