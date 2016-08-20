package com.github.pheymann.scala.bft

import akka.actor.ActorSystem
import akka.util.Timeout
import org.specs2.mutable.Specification
import org.specs2.specification.core.Fragments

import scala.concurrent.duration._

trait BftReplicaSpec extends Specification {

  val testDuration  = 6.seconds
  val testTimeout   = Timeout(testDuration)

  implicit lazy val system = ActorSystem(s"test-system-${getClass.getSimpleName}")

  override def map(fragments: => Fragments) = fragments ^ step(afterAll())

  protected def afterAll() {
    system.terminate()
  }

}
