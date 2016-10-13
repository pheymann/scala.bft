package com.github.pheymann.scala.bft.util

import java.security.{PublicKey, Signature}

import com.github.pheymann.scala.bft.Types.{DigitalSignature, Mac}

object AuthenticationDigitsVerification {

  def verifyMac(mac: Mac, check: Mac): Boolean = {
    mac.sameElements(check)
  }

  def verifyDigitalSignature(signature: DigitalSignature, publicKey: PublicKey)
                            (strategy: String): Boolean = {
    val _signature = Signature.getInstance(strategy)

    _signature.initVerify(publicKey)
    _signature.verify(signature)
  }

}
