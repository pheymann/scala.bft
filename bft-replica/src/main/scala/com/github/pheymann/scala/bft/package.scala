package com.github.pheymann.scala

package object bft {

  object Types {

    type RequestDigits  = Array[Byte]
    type Mac            = Array[Byte]
    type SessionKey     = Array[Byte]

    type DigitalSignature = Array[Byte]

    final val EmptyMac  = Array.empty[Byte]

  }

}
