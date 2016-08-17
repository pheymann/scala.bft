package com.github.pheymann.scala.bft.replica

import com.github.pheymann.scala.bft.storage.LogStorageMock

class ReplicaContextMock(
                          val replicas: ReplicasMock,
                          val storage:  LogStorageMock
                        ) extends ReplicaContext
