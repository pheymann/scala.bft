package com.github.pheymann.scala.bft.replica

import cats.{Id, ~>}
import com.github.pheymann.scala.bft.ScalaBftSpec
import com.github.pheymann.scala.bft.messaging.Sender.SenderContext
import com.github.pheymann.scala.bft.messaging._
import com.github.pheymann.scala.bft.storage.StorageAction
import com.github.pheymann.scala.bft.storage.StorageAction._

class ReplicaSpec extends ScalaBftSpec {

  val storage = new (StorageAction ~> Id) {
    private var lastView            = 0
    private var lastSequenceNumber  = 0L

    def apply[R](action: StorageAction[R]): Id[R] = action match {
      case GetLastView            => lastView
      case GetLastSequenceNumber  => lastSequenceNumber

      case IsPrePrepareStored(_) | IsPrepareStored(_) | IsCommitStored(_) => false
      case StorePrePrepare(_, _) | StorePrepare(_)  => ()

      case StoreCommit(state) =>
        lastView = state.view
        lastSequenceNumber = state.sequenceNumber
    }
  }

  "The Replica object handles consensuses by consuming the incoming message and it" should {
    """start and accept a consensus as leader if no consensus is currently running and all
      |rounds have finished""".stripMargin in {
      implicit val context = newContext(true, 0, 1)

      val sender = SenderConnection.open(1, testSessionKey, _ => (), SenderContext())
      SenderConnection.open(2, testSessionKey, _ => (), sender)
      SenderConnection.open(3, testSessionKey, _ => (), sender)

      implicit val interpreter = ServiceInterpreter.interpreter(sender, storage)

      val request = ClientRequest(0, 0L, "hello".toCharArray.map(_.toByte))

      val consensusOpt = Replica(LeaderPrePrepare(request), None)

      consensusOpt.isDefined should beTrue
      consensusOpt.get.isPrePrepared should beTrue

      Replica(PrepareMessage(1, 0, 0, 0L), consensusOpt)
      consensusOpt.get.isPrepared should beFalse

      //invalid
      Replica(PrepareMessage(1, 0, 1, 0L), consensusOpt)
      consensusOpt.get.isPrepared should beFalse

      Replica(PrepareMessage(2, 0, 0, 0L), consensusOpt)
      consensusOpt.get.isPrepared should beTrue

      Replica(PrepareMessage(3, 0, 0, 0L), consensusOpt)
      consensusOpt.get.isPrepared should beTrue

      Replica(CommitMessage(1, 0, 0, 0L), consensusOpt)
      consensusOpt.get.isCommited should beFalse

      //invalid
      Replica(CommitMessage(1, 0, 1, 0L), consensusOpt)
      consensusOpt.get.isCommited should beFalse

      Replica(CommitMessage(2, 0, 0, 0L), consensusOpt)
      consensusOpt.get.isCommited should beFalse

      Replica(CommitMessage(3, 0, 0, 0L), consensusOpt)
      consensusOpt.get.isCommited should beTrue
    }

    """start and accept a consensus as follower if no consensus is currently running, the sent
      |request is valid and all rounds have finished""".stripMargin in {
      implicit val context = newContext(false, 0, 1)

      val sender = SenderConnection.open(1, testSessionKey, _ => (), SenderContext())
      SenderConnection.open(2, testSessionKey, _ => (), sender)
      SenderConnection.open(3, testSessionKey, _ => (), sender)

      implicit val interpreter = ServiceInterpreter.interpreter(sender, storage)

      val request   = ClientRequest(0, 0L, "hello".toCharArray.map(_.toByte))
      val delivery  = RequestDelivery(1, 0, 0, 0L, request)

      //invalid
      Replica(FollowerPrePrepare(PrePrepareMessage(1, 0, 1, 0L), delivery), None) should beEqualTo(None)

      val consensusOpt = Replica(FollowerPrePrepare(PrePrepareMessage(1, 0, 0, 0L), delivery), None)

      consensusOpt.isDefined should beTrue
      consensusOpt.get.isPrePrepared should beTrue

      Replica(PrepareMessage(1, 0, 0, 0L), consensusOpt)
      consensusOpt.get.isPrepared should beFalse

      //invalid
      Replica(PrepareMessage(1, 0, 1, 0L), consensusOpt)
      consensusOpt.get.isPrepared should beFalse

      Replica(PrepareMessage(2, 0, 0, 0L), consensusOpt)
      consensusOpt.get.isPrepared should beTrue

      Replica(PrepareMessage(3, 0, 0, 0L), consensusOpt)
      consensusOpt.get.isPrepared should beTrue

      Replica(CommitMessage(1, 0, 0, 0L), consensusOpt)
      consensusOpt.get.isCommited should beFalse

      //invalid
      Replica(CommitMessage(1, 0, 1, 0L), consensusOpt)
      consensusOpt.get.isCommited should beFalse

      Replica(CommitMessage(2, 0, 0, 0L), consensusOpt)
      consensusOpt.get.isCommited should beFalse

      Replica(CommitMessage(3, 0, 0, 0L), consensusOpt)
      consensusOpt.get.isCommited should beTrue
    }
  }

}
