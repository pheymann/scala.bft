package com.github.pheymann.scala.bft.replica

import com.github.pheymann.scala.bft.consensus.{ConsensusState, PrePrepareRound}
import com.github.pheymann.scala.bft.messaging._
import org.slf4j.LoggerFactory


object Replica {

  private val log = LoggerFactory.getLogger("replica")

//  private def startForLeader(request: ClientRequest)
//                            (implicit context: ReplicaContext, processor: ReplicaProcessor): ConsensusState = {
//    val state = ConsensusState.fromContext(context)
//
//    PrePrepareRound.processLeaderPrePrepare(request, state).foldMap(processor)
//  }
//
//  private def startForFollower(message: PrePrepareMessage, delivery: RequestDelivery)
//                              (implicit context: ReplicaContext, processor: ReplicaProcessor): Option[ConsensusState] = {
//    val state = ConsensusState.fromContext(context)
//
//    PrePrepareRound.processFollowerPrePrepare(message, delivery, state).foldMap(processor)
//
//    if (state.isPrePrepared)
//      Some(state)
//    else
//      None
//  }

//  def process()
//             (implicit context: ReplicaContext, processor: ReplicaProcessor): ReplicaContext = {
//    import context.config.messageTimeout
//
//    try {
//      Await.result(context.receiverRef ? Request, context.config.messageDuration) match {
//        case (delivery: RequestDelivery, prePrepare: PrePrepareMessage) =>
//          context.consensusOpt match {
//            case Some(consensus) =>
//              ScalaBftLogger.logWarn(s"consensus already running: ${ConsensusState.toLog(consensus)}")(log)
//
//            case None =>
//              if (!context.isLeader)
//                context.consensusOpt = startForFollower(prePrepare, delivery)
//          }
//
//        case request: ClientRequest =>
//          context.consensusOpt match {
//            case Some(consensus) =>
//              ScalaBftLogger.logWarn(s"consensus already running: ${ConsensusState.toLog(consensus)}")(log)
//
//            case None =>
//              if (context.isLeader)
//                context.consensusOpt = Some(startForLeader(request))
//          }
//
//        case prepare: PrepareMessage =>
//          if (context.consensusOpt.isDefined)
//            PrepareRound.processPrepare(prepare, context.consensusOpt.get).foldMap(processor)
//
//        case commit: CommitMessage =>
//          if (context.consensusOpt.isDefined)
//            CommitRound.processCommit(commit, context.consensusOpt.get).foldMap(processor)
//
//        case NoMessage => ScalaBftLogger.logDebug("no message")(log)
//      }
//    }
//    catch {
//      case NonFatal(cause) => ScalaBftLogger.logError(cause, "error.message-consumption")(log)
//    }
//
//    context
//  }

//  def run(config: ReplicaConfig)
//         (implicit system: ActorSystem): Unit = {
//    implicit var context   = ReplicaContext(false, 0, 0L)(config)
//
//    val senderRef   = system.actorOf(Props(new SenderActor()), SenderActor.name)
//    val receiverRef = system.actorOf(Props(new ReceiverActor(senderRef)(config)), ReceiverActor.name)
//
//    val processor = ReplicaProcessor(context)
//
//    while(true)
//      context = process()(context, processor)
//  }

}
