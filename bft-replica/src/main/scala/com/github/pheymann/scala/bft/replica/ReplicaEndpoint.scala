package com.github.pheymann.scala.bft.replica

case class ReplicaEndpoint(
                            id:   Int,
                            host: String,
                            port: Int
                          ) {

  lazy val toLog = s"{$id,$host,$port}"

}
