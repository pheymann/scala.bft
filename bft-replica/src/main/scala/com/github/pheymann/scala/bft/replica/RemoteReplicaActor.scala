package com.github.pheymann.scala.bft.replica

import akka.actor.Actor
import com.github.pheymann.scala.bft.util.ActorLoggingUtil

case class RemoteReplicaActor(
                                id: Long,

                                host: String,
                                port: Int
                             )  extends Actor
                                with    ActorLoggingUtil {

  def this(data: ReplicaData) = this(data.id, data.host, data.port)

  private val remoteSelect = context.actorSelection(s"akka.tcp://scala-bft-replica@$host:$port/user/message-router")

  override def receive = {
    case message => remoteSelect ! message
  }

}
