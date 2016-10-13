package com.github.pheymann.scala.bft.util

import java.security.{MessageDigest, PrivateKey, Signature}

import com.github.pheymann.scala.bft.Types.{DigitalSignature, Mac, RequestDigits, SessionKey}
import com.github.pheymann.scala.bft.model.{ClientRequest, SignableMessage}

object AuthenticationDigitsGenerator {

  private val digitsGenerator = MessageDigest.getInstance("MD5")

  def generateDigits(request: ClientRequest): RequestDigits = {
    digitsGenerator.digest(request.body)
  }

  def generateDigits(message: SignableMessage): RequestDigits = {
    digitsGenerator.digest(message.toBytes)
  }

  def generateMAC(digits: RequestDigits, sessionKey: SessionKey): Mac = {
    digitsGenerator.digest(digits ++ sessionKey).slice(0, 10)
  }

  def generateMAC(message: SignableMessage, sessionKey: SessionKey): Mac = {
    generateMAC(generateDigits(message), sessionKey)
  }

  def generateDigitalSignature(body: Array[Byte], privateKey: PrivateKey)
                              (strategy: String): DigitalSignature = {
    val signature = Signature.getInstance(strategy)

    signature.initSign(privateKey)
    signature.update(body)
    signature.sign
  }

}
