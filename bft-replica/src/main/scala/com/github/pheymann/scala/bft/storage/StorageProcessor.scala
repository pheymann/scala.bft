package com.github.pheymann.scala.bft.storage

import cats.Id

object StorageProcessor {

  def apply[A](action: StorageAction[A]): Id[A] = action match {
    case StorePrePrepare(request, message, state) => ??? //TODO implemented storage
    case StorePrepare(message, state) => ??? //TODO implemented storage
    case StoreCommit(message, state)  => ??? //TODO implemented storage
  }

}
