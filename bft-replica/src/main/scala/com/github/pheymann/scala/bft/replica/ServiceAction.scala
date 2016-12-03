package com.github.pheymann.scala.bft.replica

import cats.free.Free

trait ServiceAction[R] {

  def liftM: Free[ServiceAction, R] = Free.liftF(this)

}

object ServiceAction {

  case object EmptyAction extends ServiceAction[Unit]

  def nothing: Free[ServiceAction, Unit] = Free.pure(EmptyAction)

}
