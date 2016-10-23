package com.github.pheymann.scala.bft.util

import java.security.{MessageDigest, PrivateKey, Signature}

import com.github.pheymann.scala.bft.{DigitalSignature, Mac, RequestDigest, SessionKey}
import com.github.pheymann.scala.bft.messaging.{ClientRequest, SignableMessage}

object AuthenticationGenerator {

  private val digestGenerator = MessageDigest.getInstance("MD5")

  def generateDigest(request: ClientRequest): RequestDigest = {
    digestGenerator.digest(request.body)
  }

  def generateDigest(message: SignableMessage): RequestDigest = {
    digestGenerator.digest(message.toBytes)
  }

  def generateMAC(digits: RequestDigest, sessionKey: SessionKey): Mac = {
    digestGenerator.digest(digits ++ sessionKey).slice(0, 10)
  }

  def generateMAC(message: SignableMessage, sessionKey: SessionKey): Mac = {
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
