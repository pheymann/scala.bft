package com.github.pheymann.scala.bft.consensus

import akka.actor.{Actor, ActorLogging}
import com.github.pheymann.scala.bft.replica.Replicas
import com.github.pheymann.scala.bft.storage.LogStorage
import com.github.pheymann.scala.bft.util.{ActorLoggingUtil, ClientRequest, RequestDigitsGenerator}

trait Consensus extends Actor
                with    ActorLogging
                with    ActorLoggingUtil {

  def request:        ClientRequest

  def sequenceNumber: Long
  def view:           Long

  protected val requestDigits = RequestDigitsGenerator.generateDigits(request)

  protected val replicas  = Replicas(context.system)
  protected val storage   = LogStorage(context.system)

  protected def isValidMessage(sequenceNumber: Long, view: Long, requestDigits: Array[Byte]): Boolean = {
    //TODO implement is valid prepare
    false
  }

}
