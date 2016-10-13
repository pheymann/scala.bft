package com.github.pheymann.scala.bft.replica

import com.github.pheymann.scala.bft.util.LoggingUtil
import com.typesafe.config.{Config, ConfigFactory}

import scala.util.control.NonFatal
import scala.collection.JavaConversions._

object StaticReplicaDiscovery extends LoggingUtil {

  private[replica] def loadReplicaData(config: StaticReplicaDiscoveryConfig): Seq[ReplicaData] = {
    try {
      config.replicas.map { entry =>
        ReplicaData(entry.getLong("id"), entry.getString("host"), entry.getInt("port"))
      }
    }
    catch {
      case NonFatal(cause) =>
        error(cause, "failed to load replica data")
        throw cause
    }
  }

}

case class StaticReplicaDiscoveryConfig(replicas: List[Config])

object StaticReplicaDiscoveryConfig {

  val config = StaticReplicaDiscoveryConfig(t())

  def t() = {
    val t = ConfigFactory.load("replica-hosts")
    println("++++++++ " + t)
    val t2: List[Config] = t.getConfigList("replicas").toList
    println("++++++++++" + t2)
    t2
  }

}

case class ReplicaData(
                        id:   Long,
                        host: String,
                        port: Int
                      )
