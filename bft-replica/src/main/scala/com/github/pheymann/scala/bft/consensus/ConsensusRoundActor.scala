package com.github.pheymann.scala.bft.consensus

import akka.actor.{Actor, ActorLogging}
import com.github.pheymann.scala.bft.replica.ReplicaContext
import com.github.pheymann.scala.bft.util.ConsensusLoggingUtil

trait ConsensusRoundActor extends Actor
                          with    ConsensusLoggingUtil {

  implicit def consensusContext:  ConsensusContext
  implicit def replicaContext:    ReplicaContext

  protected def message: ConsensusMessage

}
