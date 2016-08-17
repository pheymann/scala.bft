package com.github.pheymann.scala.bft.util

import akka.actor.Actor
import com.github.pheymann.scala.bft.consensus.CommitRound.Commit
import com.github.pheymann.scala.bft.consensus.PrePrepareRound.PrePrepare
import com.github.pheymann.scala.bft.consensus.PrepareRound.Prepare
import com.github.pheymann.scala.bft.util.CollectorStateObserver.CheckState


class StorageMessageCollectorActor(expectation: StorageMessageExpectation) extends Actor {

  import StorageMessageCollectorActor._

  var startOpt: Option[Start] = None
  var addPrePrepareOpt: Option[AddPrePrepare] = None
  var addPrepareOpt: Option[AddPrepare]       = None
  var addCommitOpt: Option[AddCommit] = None
  var finishOpt: Option[Finish]       = None

  override def receive = {
    case message: Start => startOpt = Some(message)
    case message: AddPrePrepare => addPrePrepareOpt = Some(message)
    case message: AddPrepare    => addPrepareOpt = Some(message)
    case message: AddCommit => addCommitOpt = Some(message)
    case message: Finish    => finishOpt = Some(message)

    case CheckState =>
      sender() ! {
        startOpt.isDefined == expectation.isStart &&
        addPrePrepareOpt.isDefined == expectation.isPrePrepare &&
        addPrepareOpt.isDefined == expectation.isPrepare &&
        addCommitOpt.isDefined  == expectation.isCommit &&
        finishOpt.isDefined     == expectation.isFinish
      }
  }

}

object StorageMessageCollectorActor {

  case class Start(request: ClientRequest)
  case class AddPrePrepare(message: PrePrepare)
  case class AddPrepare(message: Prepare)
  case class AddCommit(message: Commit)
  case class Finish(message: Commit)

}
