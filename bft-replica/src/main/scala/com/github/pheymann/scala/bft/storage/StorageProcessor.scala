package com.github.pheymann.scala.bft.storage

import cats.Id

object StorageProcessor {

  def apply[A](action: StorageAction[A]): Id[A] = action match {
    case StorePrePrepare(request, message, state) => ???
    case StorePrepare(message, state) => ???
    case StoreCommit(message, state)  => ???
  }

}
