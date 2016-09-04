package com.github.pheymann.scala.bft.replica.messaging

import akka.actor.{ActorNotFound, Props}
import com.github.pheymann.scala.bft.model.{ClientRequest, DataChunk, RequestDelivery, StartChunkStream}
import com.github.pheymann.scala.bft.{BftReplicaSpec, WithActorSystem}
import org.specs2.concurrent.ExecutionEnv

class RequestBrokerActorSpec(implicit ee: ExecutionEnv) extends BftReplicaSpec {

  sequential

  "The RequestBroker" should {
    "create a new DataChunkStream on request and abort already running instances if necessary" in new WithActorSystem("create-receiver") {
      val brokerRef = system.actorOf(Props(new RequestBrokerActor(self)), "test.broker")

      within(testDuration) {
        def checkStreamCreation() = {
          brokerRef ! StartChunkStream(0, 1)

          system
            .actorSelection("/user/test.broker/request.chunk.stream.0")
            .resolveOne(testDuration * 2) should not (throwA[ActorNotFound]).awaitFor(testDuration * 2)
        }

        checkStreamCreation()
        checkStreamCreation()
      }
    }

    "forward all chunks to the stream receiver until the request is complete" in new WithActorSystem {
      val brokerRef       = system.actorOf(Props(new RequestBrokerActor(self)))
      val requestDelivery = RequestDelivery(0, 0, ClientRequest(0, 0, Array[Byte](1, 2, 3)))
      val chunks          = ChunkDataStreamSender.generateChunks(requestDelivery)

      within(testDuration) {
        brokerRef ! StartChunkStream(0, chunks.length)

        for (chunk <- chunks)
          brokerRef ! DataChunk(0, chunk)

        expectMsgPF() {
          case delivery: RequestDelivery =>
            delivery.sequenceNumber === requestDelivery.sequenceNumber
            delivery.view           === requestDelivery.view

            delivery.request.clientId   === requestDelivery.request.clientId
            delivery.request.timestamp  === requestDelivery.request.timestamp
            delivery.request.body.sameElements(requestDelivery.request.body) should beTrue
        }
      }
    }
  }

}
