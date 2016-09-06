package com.github.pheymann.scala.bft.util

import java.security.{PublicKey, Signature}

import com.github.pheymann.scala.bft.BftReplicaConfig
import com.github.pheymann.scala.bft.Types.DigitalSignature

object AuthenticationDigitsVerification {

  def verifyDigitalSignature(signature: DigitalSignature, publicKey: PublicKey): Boolean = {
    val _signature = Signature.getInstance(BftReplicaConfig.signatureStrategy)

    _signature.initVerify(publicKey)
    _signature.verify(signature)
  }

}
