package com.github.pheymann.scala.bft.replica

import com.github.pheymann.scala.bft.BftReplicaConfig
import com.github.pheymann.scala.bft.util.LoggingUtil
import com.typesafe.config.ConfigFactory

import scala.collection.JavaConversions._
import scala.util.control.NonFatal

object StaticReplicaDiscovery extends LoggingUtil {

  private val config = ConfigFactory.load(BftReplicaConfig.replicaHostFile)

  val replicaData = loadReplicaData()

  private def loadReplicaData(): Seq[ReplicaData] = {
    try {
      config.getStringList("replicas").toList.map { entry =>
        val data = entry.split(":")

        ReplicaData(data(0).toLong, data(1), data(2).toInt)
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
