package com.github.pheymann.scala.bft.util

case class StorageMessageExpectation(
                                      isStart: Boolean = false,
                                      isPrePrepare: Boolean = false,
                                      isPrepare: Boolean = false,
                                      isCommit: Boolean = false,
                                      isFinish: Boolean = false
                                    )

object StorageMessageExpectation {

  def forValidConsensus: StorageMessageExpectation = {
    StorageMessageExpectation(true, true, true, true, true)
  }

}
