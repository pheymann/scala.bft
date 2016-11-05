package com.github.pheymann.scala.bft.util

import com.github.pheymann.scala.bft.SessionKey
import com.github.pheymann.scala.bft.messaging.{SignedConsensusMessage, SignedRequestChunk}
import com.github.pheymann.scala.bft.replica.ReplicaConfig

object AuthenticationVerification {

  import AuthenticationGenerator._

  def verify(message: SignedConsensusMessage, sessionKey: SessionKey)
            (implicit config: ReplicaConfig): Boolean = {
    message.mac.sameElements(generateMAC(generateDigest(message.message), sessionKey))
  }

  def verify(chunk: SignedRequestChunk, sessionKey: SessionKey)
            (implicit config: ReplicaConfig): Boolean = {
    chunk.mac.sameElements(generateMAC(generateDigest(chunk.chunk), sessionKey))
  }

}
