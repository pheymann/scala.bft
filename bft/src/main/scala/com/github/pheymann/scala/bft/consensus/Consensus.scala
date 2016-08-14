package com.github.pheymann.scala.bft.consensus

import akka.actor.{Actor, ActorLogging}
import com.github.pheymann.scala.bft.util.ActorLoggingUtil

class Consensus(
                 sequenceNumber:  Long,
                 view:            Long
               )  extends Actor
                  with    ActorLogging
                  with    ActorLoggingUtil
                  with    PrePrepareRound {

  override def receive = prePrepare

}
