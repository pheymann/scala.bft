package com.github.pheymann.scala.bft.consensus

import akka.actor.{Actor, ActorLogging}
import com.github.pheymann.scala.bft.replica.Replicas
import com.github.pheymann.scala.bft.storage.LogStorage
import com.github.pheymann.scala.bft.util.ActorLoggingUtil

trait ConsensusActor  extends Actor
                      with    ActorLogging
                      with    ActorLoggingUtil {

  implicit def consensusContext: ConsensusContext

  protected val replicas  = Replicas(context.system)
  protected val storage   = LogStorage(context.system)

}
