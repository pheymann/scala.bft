package com.github.pheymann.scala.bft.replica

import com.github.pheymann.scala.bft.ScalaBftSpec

class ReplicaDiscoverySpec extends ScalaBftSpec {

  "Replica discovery" should {
    "provide a function to load replica endpoints from a static file" in {
      ReplicaDiscovery.fromConfig("replica-endpoints") should beEqualTo(Seq(
        ReplicaEndpoint(0, "127.0.0.1", 9001),
        ReplicaEndpoint(1, "127.0.0.2", 9002)
      ))
    }
  }

}
