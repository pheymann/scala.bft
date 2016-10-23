package com.github.pheymann.scala.bft.messaging

case class ClientRequest(
                          clientId:   Long,
                          timestamp:  Long,
                          body:       Array[Byte]
                        ) {

  lazy val toLog = s"{$clientId,$timestamp}"

}
