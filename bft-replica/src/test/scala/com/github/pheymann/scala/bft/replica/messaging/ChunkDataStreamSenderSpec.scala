package com.github.pheymann.scala.bft.replica.messaging

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import com.github.pheymann.scala.bft.model.{ClientRequest, DataChunk, RequestDelivery, StartChunkStream}
import com.github.pheymann.scala.bft.{BftReplicaSpec, WithActorSystem}

class ChunkDataStreamSenderSpec extends BftReplicaSpec {

  import RemoteReplicaMockActor._

  "The ChunkDataStreamSender" should {
    "send generate chunks out of a request and send them to all remote replicas" in new WithActorSystem {
      val remoteRefs      = createRemoteRefs(self, 2)
      val requestDelivery = RequestDelivery(0, 0, ClientRequest(0, 0, Array[Byte](1, 2, 3)))
      val numberOfChunks  = ChunkDataStreamSender.generateChunks(requestDelivery).length

      within(testDuration) {
        ChunkDataStreamSender.send(0, requestDelivery, remoteRefs)

        expectMsgAllOf(ReceivedStartStream, ReceivedStartStream)
        expectMsgAllOf(Seq.fill(numberOfChunks * 2)(ReceivedChunk): _*)

        expectNoMsg(noMessageDuration)
      }
    }
  }

  def createRemoteRefs(specRef: ActorRef, numberOfRefs: Int)
                      (implicit system: ActorSystem): Seq[ActorRef] = {
    def createRemoteRef(): ActorRef = system.actorOf(Props(new RemoteReplicaMockActor(specRef)))

    Seq.fill(numberOfRefs)(createRemoteRef())
  }

  class RemoteReplicaMockActor(specRef: ActorRef) extends Actor {

    override def receive = {
      case _: StartChunkStream  => specRef ! ReceivedStartStream
      case _: DataChunk         => specRef ! ReceivedChunk
    }

  }

  object RemoteReplicaMockActor {

    case object ReceivedStartStream
    case object ReceivedChunk

  }

}
