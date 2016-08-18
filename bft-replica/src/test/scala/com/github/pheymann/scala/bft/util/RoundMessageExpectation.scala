package com.github.pheymann.scala.bft.util

case class RoundMessageExpectation(
                                    prePrepareNumber: Int = 0,
                                    prepareNumber:    Int = 0,
                                    commitNumber:     Int = 0,

                                    isRequestDelivery: Boolean = false
                                  )
