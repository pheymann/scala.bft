package com.github.pheymann.scala.bft.messaging

import com.github.pheymann.scala.bft.replica.ReplicaConfig

trait Send {

  def send[T](message: T)
             (implicit config: ReplicaConfig): Unit

}

object Send {

  implicit val actorSend = new Send {

    def send[T](message: T)
            (implicit config: ReplicaConfig): Unit = {
      config.senderRef ! message
    }

  }

}
