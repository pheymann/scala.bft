package com.github.pheymann.scala.bft

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.Timeout
import org.specs2.mutable.{After, Specification}

import scala.concurrent.duration._

abstract class WithActorSystem  extends TestKit(ActorSystem())
                                with    After
                                with    ImplicitSender {

  def after = system.terminate()

}


trait BftReplicaSpec extends Specification {

  val testDuration  = 6.seconds
  val testTimeout   = Timeout(testDuration)

}
