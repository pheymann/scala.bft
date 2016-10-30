package com.github.pheymann.scala.bft.util

import com.github.pheymann.scala.bft.SessionKey
import com.github.pheymann.scala.bft.messaging.SignedRequestChunk
import com.github.pheymann.scala.bft.replica.ReplicaConfig

object AuthenticationVerification {

  import AuthenticationGenerator._

  def verify(chunk: SignedRequestChunk, sessionKey: SessionKey)
            (implicit config: ReplicaConfig): Boolean = {
    chunk.mac.sameElements(generateMAC(generateDigest(chunk.chunk), sessionKey))
  }

}
