package com.github.pheymann.scala.bft.messaging

import com.github.pheymann.scala.bft.replica.ReplicaEndpoint

object SenderActor {

  def url(endpoint: ReplicaEndpoint, receiverName: String): String = {
    s"akka.tcp://scala-bft-replica@%s:%d/user/%s".format(
      endpoint.host,
      endpoint.port,
      receiverName
    )
  }

}
