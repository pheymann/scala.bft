package com.github.pheymann.scala.bft.util

import akka.actor.{Actor, ActorLogging, ActorRef}
import com.github.pheymann.scala.bft.consensus.CommitRound.Commit
import com.github.pheymann.scala.bft.consensus.PrePrepareRound.PrePrepare
import com.github.pheymann.scala.bft.consensus.PrepareRound.Prepare
import com.github.pheymann.scala.bft.util.CollectorStateObserver.LogCollectorReady


class StorageMessageCollectorActor(observerRef: ActorRef) extends Actor
                                                          with    ActorLogging
                                                          with    ActorLoggingUtil {

  import StorageMessageCollectorActor._

  var expectation: StorageMessageExpectation = _

  var startOpt: Option[Start] = None
  var addPrePrepareOpt: Option[AddPrePrepare] = None
  var addPrepareOpt: Option[AddPrepare]       = None
  var addCommitOpt: Option[AddCommit] = None
  var finishOpt: Option[Finish]       = None

  override def receive = {
    case InitStorageCollector(expect) =>
      expectation = expect
      debug(s"expectation: $expect")

    case message: Start =>
      startOpt = Some(message)
      sendStateIfReady()
    case message: AddPrePrepare =>
      addPrePrepareOpt = Some(message)
      sendStateIfReady()
    case message: AddPrepare =>
      addPrepareOpt = Some(message)
      sendStateIfReady()
    case message: AddCommit =>
      addCommitOpt = Some(message)
      sendStateIfReady()
    case message: Finish =>
      finishOpt = Some(message)
      sendStateIfReady()
  }

  private def sendStateIfReady() {
    debug("start: %s, pre-prepare: %s, prepare: %s, commit: %s, finish: %s".format(
      startOpt.isDefined,
      addPrePrepareOpt.isDefined,
      addPrepareOpt.isDefined,
      addCommitOpt.isDefined,
      finishOpt.isDefined
    ))

    if (
      startOpt.isDefined == expectation.isStart &&
      addPrePrepareOpt.isDefined == expectation.isPrePrepare &&
      addPrepareOpt.isDefined == expectation.isPrepare &&
      addCommitOpt.isDefined  == expectation.isCommit &&
      finishOpt.isDefined     == expectation.isFinish
    )
      observerRef ! LogCollectorReady
  }

}

object StorageMessageCollectorActor {

  case class InitStorageCollector(expectation: StorageMessageExpectation)

  case class Start(request: ClientRequest)
  case class AddPrePrepare(message: PrePrepare)
  case class AddPrepare(message: Prepare)
  case class AddCommit(message: Commit)
  case class Finish(message: Commit)

}
