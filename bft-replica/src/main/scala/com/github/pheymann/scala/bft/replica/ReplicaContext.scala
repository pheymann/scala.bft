package com.github.pheymann.scala.bft.replica

final case class ReplicaContext(
                                isLeader:       Boolean,
                                view:           Int,

                                var sequenceNumber: Long
                               )(
                                implicit val config: ReplicaConfig
                               )
