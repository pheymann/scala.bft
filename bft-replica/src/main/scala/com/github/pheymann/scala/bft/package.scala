package com.github.pheymann.scala

package object bft {

  type SessionKey     = Array[Byte]
  type Mac            = Array[Byte]
  type RequestDigest  = Array[Byte]

  type DigitalSignature = Array[Byte]

}
