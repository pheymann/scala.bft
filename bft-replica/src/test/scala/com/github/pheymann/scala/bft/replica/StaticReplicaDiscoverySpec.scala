package com.github.pheymann.scala.bft.replica

import com.github.pheymann.scala.bft.BftReplicaSpec
import com.typesafe.config.{ConfigException, ConfigFactory}

class StaticReplicaDiscoverySpec extends BftReplicaSpec {

  "The StaticReplicaDiscovery" should {
    "load all replicas participating in the system from a config file" in {
      val replicaData = StaticReplicaDiscovery.loadReplicaData(ConfigFactory.load("valid-replica-hosts"))

      replicaData === Seq(ReplicaData(1, "127.0.0.1", 1000), ReplicaData(2, "127.0.0.2", 1001))
    }

    "break if the config is invalid" in {
      StaticReplicaDiscovery.loadReplicaData(ConfigFactory.load("invalid-replica-hosts")) should throwA[ConfigException]
    }
  }

}
