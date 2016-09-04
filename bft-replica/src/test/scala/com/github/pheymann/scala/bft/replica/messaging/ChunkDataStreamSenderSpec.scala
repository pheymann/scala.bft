package com.github.pheymann.scala.bft.replica.messaging

import akka.actor.{ActorRef, ActorSystem, Props}
import com.github.pheymann.scala.bft.model.{ClientRequest, RequestDelivery}
import com.github.pheymann.scala.bft.replica.RemoteReplicaActorMock
import com.github.pheymann.scala.bft.replica.RemoteReplicaActorMock.{ReceivedDataChunk, ReceivedStartStream}
import com.github.pheymann.scala.bft.{BftReplicaSpec, WithActorSystem}

class ChunkDataStreamSenderSpec extends BftReplicaSpec {

  "The ChunkDataStreamSender" should {
    "send generate chunks out of a request and send them to all remote replicas" in new WithActorSystem {
      val remoteRefs      = createRemoteRefs(self, 2)
      val requestDelivery = RequestDelivery(0, 0, ClientRequest(0, 0, Array[Byte](1, 2, 3)))
      val numberOfChunks  = ChunkDataStreamSender.generateChunks(requestDelivery).length

      within(testDuration) {
        ChunkDataStreamSender.send(0, requestDelivery, remoteRefs)

        expectMsgAllOf(ReceivedStartStream(numberOfChunks), ReceivedStartStream(numberOfChunks))
        expectMsgAllOf(Seq.fill(numberOfChunks * 2)(ReceivedDataChunk): _*)

        expectNoMsg(noMessageDuration)
      }
    }
  }

  def createRemoteRefs(specRef: ActorRef, numberOfRefs: Int)
                      (implicit system: ActorSystem): Seq[ActorRef] = {
    def createRemoteRef(): ActorRef = system.actorOf(Props(new RemoteReplicaActorMock(specRef)))

    Seq.fill(numberOfRefs)(createRemoteRef())
  }

}
