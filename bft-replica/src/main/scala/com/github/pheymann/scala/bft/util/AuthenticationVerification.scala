package com.github.pheymann.scala.bft.util

import com.github.pheymann.scala.bft.SessionKey
import com.github.pheymann.scala.bft.messaging.RequestChunk
import com.github.pheymann.scala.bft.replica.ReplicaConfig

object AuthenticationVerification {

  import AuthenticationGenerator._

  def verify(chunk: RequestChunk, sessionKey: SessionKey)
            (implicit config: ReplicaConfig): Boolean = {
    chunk.mac.sameElements(generateMAC(generateDigest(chunk), sessionKey))
  }

}
