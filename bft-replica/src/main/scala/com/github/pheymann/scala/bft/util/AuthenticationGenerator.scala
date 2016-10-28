package com.github.pheymann.scala.bft.util

import java.security.{PrivateKey, Signature}

import com.github.pheymann.scala.bft.{DigitalSignature, Mac, RequestDigest, SessionKey}
import com.github.pheymann.scala.bft.messaging.{RequestChunk, SignableMessage}
import com.github.pheymann.scala.bft.replica.ReplicaConfig

object AuthenticationGenerator {

  def generateDigest(chunk: RequestChunk)
                    (implicit config: ReplicaConfig): RequestDigest = {
    config.digestGenerator.digest(chunk.chunk)
  }

  def generateDigest(message: SignableMessage)
                    (implicit config: ReplicaConfig): RequestDigest = {
    config.digestGenerator.digest(message.toBytes)
  }

  def generateMAC(digits: RequestDigest, sessionKey: SessionKey)
                 (implicit config: ReplicaConfig): Mac = {
    config.digestGenerator.digest(digits ++ sessionKey).slice(0, 10)
  }

  def generateMAC(message: SignableMessage, sessionKey: SessionKey)
                 (implicit config: ReplicaConfig): Mac = {
    generateMAC(generateDigest(message), sessionKey)
  }

  def generateDigitalSignature(body: Array[Byte], privateKey: PrivateKey)
                              (strategy: String): DigitalSignature = {
    val signature = Signature.getInstance(strategy)

    signature.initSign(privateKey)
    signature.update(body)
    signature.sign
  }

}
