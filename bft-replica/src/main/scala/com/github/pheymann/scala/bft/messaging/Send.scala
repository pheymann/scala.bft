package com.github.pheymann.scala.bft.messaging

import com.github.pheymann.scala.bft.replica.ReplicaContext

trait Send {

  def send[T](message: T)
             (implicit context: ReplicaContext): Unit

}

object Send {

  implicit val actorSend = new Send {

    def send[T](message: T)
            (implicit context: ReplicaContext): Unit = {
      context.senderRef ! message
    }

  }

}
