package com.github.pheymann.scala.bft.util

import java.security.{MessageDigest, PrivateKey, Signature}

import com.github.pheymann.scala.bft.{DigitalSignature, Mac, RequestDigest, SessionKey}
import com.github.pheymann.scala.bft.messaging.SignableMessage
import com.github.pheymann.scala.bft.replica.ReplicaConfig

object AuthenticationGenerator {

  private def digestGenerator(implicit config: ReplicaConfig) = {
    MessageDigest.getInstance(config.digestStrategy)
  }

  def generateDigest(chunk: Array[Byte])
                    (implicit config: ReplicaConfig): RequestDigest = {
    digestGenerator.digest(chunk)
  }

  def generateDigest(message: SignableMessage)
                    (implicit config: ReplicaConfig): RequestDigest = {
    digestGenerator.digest(message.toBytes)
  }

  def generateMAC(digest: RequestDigest, sessionKey: SessionKey)
                 (implicit config: ReplicaConfig): Mac = {
    digestGenerator.digest(digest ++ sessionKey).slice(0, 10)
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
