package com.github.pheymann.scala.bft.util

import com.github.pheymann.scala.bft.model.ClientRequest

object RequestDigitsGenerator {

  def generateDigits(request: ClientRequest): Array[Byte] = {
    //TODO implement generate digits
    request.body
  }

}
