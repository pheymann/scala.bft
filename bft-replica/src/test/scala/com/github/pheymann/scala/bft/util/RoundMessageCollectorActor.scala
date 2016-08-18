package com.github.pheymann.scala.bft.util

import akka.actor.{Actor, ActorLogging}
import com.github.pheymann.scala.bft.consensus.CommitRound.Commit
import com.github.pheymann.scala.bft.consensus.ConsensusMessage
import com.github.pheymann.scala.bft.consensus.PrePrepareRound.PrePrepare
import com.github.pheymann.scala.bft.consensus.PrepareRound.Prepare
import com.github.pheymann.scala.bft.util.CollectorStateObserver.CheckState

class RoundMessageCollectorActor  extends Actor
                                  with    ActorLogging
                                  with    ActorLoggingUtil {

  import RoundMessageCollectorActor._

  var expectation: RoundMessageExpectation = _

  val prePrepareBuffer = collection.mutable.ListBuffer[PrePrepare]()
  val prepareBuffer    = collection.mutable.ListBuffer[Prepare]()
  val commitBuffer     = collection.mutable.ListBuffer[Commit]()

  var requestDeliveryOpt: Option[RequestDeliveryMock] = None

  override def receive = {
    case InitRoundCollector(expect) =>
      expectation = expect
      debug(s"expectation: $expect")

    case message: PrePrepare => prePrepareBuffer += message
    case message: Prepare => prepareBuffer += message
    case message: Commit => commitBuffer += message

    case message: RequestDeliveryMock => requestDeliveryOpt = Some(message)

    case CheckState =>
      debug(s"pre-prepare: %d, prepare: %d, commit: %d, is request: %s".format(
        prePrepareBuffer.length,
        prepareBuffer.length,
        commitBuffer.length,
        requestDeliveryOpt.isDefined
      ))

      sender() ! {
        prePrepareBuffer.length == expectation.prePrepareNumber &&
        prepareBuffer.length    == expectation.prepareNumber &&
        commitBuffer.length     == expectation.commitNumber &&
        requestDeliveryOpt.isDefined == expectation.isRequestDelivery
      }
  }

}

object RoundMessageCollectorActor {

  case class InitRoundCollector(expectation: RoundMessageExpectation)
  case class RequestDeliveryMock(message: ConsensusMessage, request: ClientRequest)

}
