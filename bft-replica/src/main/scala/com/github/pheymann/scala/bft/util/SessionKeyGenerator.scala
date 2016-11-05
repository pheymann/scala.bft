package com.github.pheymann.scala.bft.util

import com.github.pheymann.scala.bft.SessionKey

object SessionKeyGenerator {

  def generateSessionKey(senderId: Int, selfId: Int): SessionKey = {
    //TODO implement session key generator
    (0 until 16).map(_.toByte).toArray
  }

}
