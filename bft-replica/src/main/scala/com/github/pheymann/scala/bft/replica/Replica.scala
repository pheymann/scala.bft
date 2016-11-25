package com.github.pheymann.scala.bft.replica

import com.github.pheymann.scala.bft.consensus.{CommitRound, ConsensusState, PrePrepareRound, PrepareRound}
import com.github.pheymann.scala.bft.messaging._

object Replica {

  def handle(msg: ScalaBftMessage, stateOpt: Option[ConsensusState])
            (implicit context: ReplicaContext, interpreter: ServiceInterpreter): Option[ConsensusState] = msg match {
    case LeaderPrePrepare(request) =>
      val state = ConsensusState.fromContext(context)

      Option(PrePrepareRound.processLeaderPrePrepare(request, state).foldMap(interpreter))

    case FollowerPrePrepare(prePrepare, delivery) =>
      val state = ConsensusState.fromContext(context)

      PrePrepareRound.processFollowerPrePrepare(prePrepare, delivery, state).foldMap(interpreter)

      if (state.isPrePrepared)
        Some(state)
      else
        None


    case prepare: PrepareMessage  => stateOpt.map(PrepareRound.processPrepare(prepare, _).foldMap(interpreter))
    case commit:  CommitMessage   => stateOpt.map(CommitRound.processCommit(commit, _).foldMap(interpreter))
  }

}
