package com.github.pheymann.scala.bft.replica

import com.github.pheymann.scala.bft.BftReplicaConfig
import com.github.pheymann.scala.bft.util.LoggingUtil
import com.typesafe.config.{Config, ConfigFactory}

import scala.collection.JavaConversions._
import scala.util.control.NonFatal

object StaticReplicaDiscovery extends LoggingUtil {

  lazy val replicaData = loadReplicaData(ConfigFactory.load(BftReplicaConfig.replicaHostFile))

  private[replica] def loadReplicaData(config: Config): Seq[ReplicaData] = {
    try {
      config.getConfigList("replicas").toList.map { entry =>
        ReplicaData(entry.getLong("id"), entry.getString("host"), entry.getInt("port"))
      }
    }
    catch {
      case NonFatal(cause) =>
        error(cause, "failed to create replica date")
        throw cause
    }
  }

}

case class ReplicaData(
                        id:   Long,
                        host: String,
                        port: Int
                      )
