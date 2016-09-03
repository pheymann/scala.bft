package com.github.pheymann.scala.bft.replica

import akka.actor.{ActorRef, ActorSystem}
import com.github.pheymann.scala.bft.storage.LogStorageMock

class ReplicaContextMock(
                          val replicas: ReplicasMock,
                          val storage:  LogStorageMock,

                          publisherRef: ActorRef
                        )(
                          implicit system: ActorSystem
                        ) extends ReplicaContext {

  val messaging = Messaging(publisherRef, system)

}
