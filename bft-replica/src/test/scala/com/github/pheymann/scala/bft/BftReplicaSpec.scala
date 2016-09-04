package com.github.pheymann.scala.bft

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import org.specs2.mutable.{After, Specification}

import scala.concurrent.duration._

abstract class WithActorSystem(name: String = "default")  extends TestKit(ActorSystem(name))
                                                          with    After
                                                          with    ImplicitSender {

  def after = system.terminate()

}


trait BftReplicaSpec extends Specification {

  val noMessageDuration = 500.milliseconds
  val testDuration      = 5.seconds

}
