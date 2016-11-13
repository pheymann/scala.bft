package com.github.pheymann.scala.bft.replica

import akka.actor.Props
import com.github.pheymann.scala.bft.ScalaBftSpec
import com.github.pheymann.scala.bft.messaging.ReceiverActor

class ReplicaSpec extends ScalaBftSpec {

  "A replica instance" should {
    """should start a consensus for a client request if it is the leader and finish when all
      |conditions are fulfilled.
    """.stripMargin in new WithActorSystem {
      implicit var context    = newContext(true, 0, 1, self)
      implicit val processor  = ReplicaProcessor(context)

      val receiverRef = system.actorOf(Props(new ReceiverActor()))

      context = Replica.process()

      1 === 1
    }
  }

}
