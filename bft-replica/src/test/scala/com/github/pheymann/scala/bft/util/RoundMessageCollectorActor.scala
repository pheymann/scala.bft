package com.github.pheymann.scala.bft.util

import akka.actor.Actor
import com.github.pheymann.scala.bft.consensus.CommitRound.Commit
import com.github.pheymann.scala.bft.consensus.ConsensusMessage
import com.github.pheymann.scala.bft.consensus.PrePrepareRound.PrePrepare
import com.github.pheymann.scala.bft.consensus.PrepareRound.Prepare

class RoundMessageCollectorActor extends Actor {

  import RoundMessageCollectorActor._

  lazy val prePrepareBuffer = collection.mutable.ListBuffer[PrePrepare]()
  lazy val prepareBuffer    = collection.mutable.ListBuffer[Prepare]()
  lazy val commitBuffer     = collection.mutable.ListBuffer[Commit]()

  var requestDeliveryOpt: Option[RequestDeliveryMock] = None

  override def receive = {
    case message: PrePrepare => prePrepareBuffer += message
    case message: Prepare => prepareBuffer += message
    case message: Commit => commitBuffer += message

    case message: RequestDeliveryMock => requestDeliveryOpt = Some(message)
  }

}

object RoundMessageCollectorActor {

  case class RequestDeliveryMock(message: ConsensusMessage, request: ClientRequest)

}
