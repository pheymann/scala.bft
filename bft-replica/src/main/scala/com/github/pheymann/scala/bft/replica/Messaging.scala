package com.github.pheymann.scala.bft.replica

import akka.actor.{ActorRef, ActorSystem, Props}

trait Messaging {

  def messageBrokerRef: ActorRef
  def requestBrokerRef: ActorRef

  def router: ActorRef

}

object Messaging {

  def apply(publisherRef: ActorRef, system: ActorSystem): Messaging = new MessagingExtension(publisherRef)(system)

}

class MessagingExtension(publisherRef: ActorRef)
                        (implicit system: ActorSystem) extends Messaging {

  override val messageBrokerRef = system.actorOf(Props(new MessageBrokerActor()))
  override val requestBrokerRef = system.actorOf(Props(new RequestBrokerActor(publisherRef)))

  override val router = system.actorOf(Props(new MessageRouterActor(messageBrokerRef, requestBrokerRef)))

}
