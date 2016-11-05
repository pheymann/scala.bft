package com.github.pheymann.scala.bft.replica

import com.typesafe.config.ConfigFactory

import scala.collection.JavaConversions._

object ReplicaDiscovery {

  case object DiscoverReplicas extends ReplicaAction[Seq[ReplicaEndpoint]]

  def fromConfig(configFile: String): Seq[ReplicaEndpoint] = {
    val config = ConfigFactory.load(configFile)

    config.getConfigList("replicas").toSeq.map { endpointConfig =>
      ReplicaEndpoint(
        endpointConfig.getInt("id"),
        endpointConfig.getString("host"),
        endpointConfig.getInt("port")
      )
    }
  }

}
