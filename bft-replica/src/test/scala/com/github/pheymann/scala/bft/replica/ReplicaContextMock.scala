package com.github.pheymann.scala.bft.replica

import akka.actor.{ActorSystem, Props}
import com.github.pheymann.scala.bft.storage.LogStorageMock
import com.github.pheymann.scala.bft.util.RoundMessageCollectorActor

class ReplicaContextMock(
                          val self:     Replica,
                          val storage:  LogStorageMock
                        )(
                          implicit system: ActorSystem
                        ) extends ReplicaContext {

  implicit val roundMessageRef = system.actorOf(Props(new RoundMessageCollectorActor()))

  override val replicas = new ReplicasMock(self)

}
