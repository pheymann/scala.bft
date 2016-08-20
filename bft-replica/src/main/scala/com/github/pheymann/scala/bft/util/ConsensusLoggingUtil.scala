package com.github.pheymann.scala.bft.util

import com.github.pheymann.scala.bft.consensus.ConsensusRoundActor

trait ConsensusLoggingUtil extends ActorLoggingUtil { this: ConsensusRoundActor =>

  protected def round: String

  def infoQuery(keys: String*) {
    info(s"${message.toLog}.$round.${keys.mkString(".")}")
  }

  def debugQuery(keys: String*) {
    debug(s"${message.toLog}.$round.${keys.mkString(".")}")
  }

}
