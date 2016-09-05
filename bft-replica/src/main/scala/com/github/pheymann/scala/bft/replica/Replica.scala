package com.github.pheymann.scala.bft.replica

class Replica(
              val id:   Long,

              var view: Long,
              var sequenceNumber: Long //TODO check if there is concurrent access
             ) {

  def this(data: ReplicaData) = this(data.id, 0L, 0L)

}
