package com.github.pheymann.scala.bft.consensus

import akka.actor.{Actor, ActorLogging}
import com.github.pheymann.scala.bft.replica.ReplicaContext
import com.github.pheymann.scala.bft.util.ActorLoggingUtil

trait ConsensusRoundActor extends Actor
                          with    ActorLogging
                          with    ActorLoggingUtil {

  implicit def consensusContext:  ConsensusContext
  implicit def replicaContext:    ReplicaContext

  protected val replicas  = replicaContext.replicas
  protected val storage   = replicaContext.storage

}
