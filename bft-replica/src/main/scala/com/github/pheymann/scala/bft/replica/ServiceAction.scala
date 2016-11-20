package com.github.pheymann.scala.bft.replica

import cats.free.Free

trait ServiceAction[R]

object ServiceAction {

  case object EmptyAction extends ServiceAction[Unit]

  def empty: Free[ServiceAction, Unit] = Free.liftF(EmptyAction)

}
