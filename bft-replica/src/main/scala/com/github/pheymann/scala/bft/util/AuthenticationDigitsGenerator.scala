package com.github.pheymann.scala.bft.util

import java.security.{MessageDigest, PrivateKey, Signature}

import com.github.pheymann.scala.bft.BftReplicaConfig
import com.github.pheymann.scala.bft.Types.{DigitalSignature, Mac, RequestDigits, SessionKey}
import com.github.pheymann.scala.bft.model.ClientRequest

object AuthenticationDigitsGenerator {

  private val digitsGenerator = MessageDigest.getInstance("MD5")

  def generateDigits(request: ClientRequest): RequestDigits = {
    digitsGenerator.digest(request.body)
  }

  def generateMAC(digits: RequestDigits, sessionKey: SessionKey): Mac = {
    digitsGenerator.digest(digits ++ sessionKey).slice(0, 10)
  }

  def generateDigitalSignature(body: Array[Byte], privateKey: PrivateKey): DigitalSignature = {
    val signature = Signature.getInstance(BftReplicaConfig.signatureStrategy)

    signature.initSign(privateKey)
    signature.update(body)
    signature.sign
  }

}
