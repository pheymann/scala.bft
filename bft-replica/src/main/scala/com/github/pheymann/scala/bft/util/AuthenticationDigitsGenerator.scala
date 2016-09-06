package com.github.pheymann.scala.bft.util

import java.security.MessageDigest

import com.github.pheymann.scala.bft.Types.{Mac, RequestDigits, SessionKey}
import com.github.pheymann.scala.bft.model.ClientRequest

object AuthenticationDigitsGenerator {

  private val digitsGenerator = MessageDigest.getInstance("MD5")

  def generateDigits(request: ClientRequest): RequestDigits = {
    digitsGenerator.digest(request.body)
  }

  def generateMAC(digits: RequestDigits, sessionKey: SessionKey): Mac = {
    digitsGenerator.digest(digits ++ sessionKey).slice(0, 10)
  }

}
