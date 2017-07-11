package ru.fan

import java.io.Closeable
import java.nio.file.{Path, Paths}

import ru.fan.stockexchange.model._
import ru.fan.stockexchange.services.stockexchange.StockExchange

import scala.collection.mutable
import scala.language.implicitConversions

package object stockexchange {
  // Implicit builder for java.nio.file.Path for scopt library
  implicit val pathRead: scopt.Read[Path] = scopt.Read.reads {p => Paths.get(p)}
  
  trait Managed[T] {
    def onEnter(): T
    def onExit(t:Throwable = null)
    def attempt(block: => Unit) {
      try { block } finally {}
    }
  }

  // Implementation of a managed resource block like java try-with-resource
  def using[T <: Any, R](managed: Managed[T])(block: T => R): R = {
    val resource = managed.onEnter()
    var exception = false
    try {
      block(resource)
    } catch  {
      case t:Throwable =>
        exception = true
        managed.onExit(t)
        throw t
        
    } finally {
      if (!exception) {
        managed.onExit()
      }
    }
  }

  def using[T <: Any, U <: Any, R] (managed1: Managed[T], managed2: Managed[U]) (block: T => U => R): R = {
    using[T, R](managed1) { r =>
      using[U, R](managed2) { s => block(r)(s) }
    }
  }

  class ManagedClosable[T <: Closeable](closable:T) extends Managed[T] {
    def onEnter(): T = closable
    def onExit(t:Throwable = null) {
      attempt(closable.close())
    }
  }

  implicit def closable2managed[T <: Closeable](closable:T): Managed[T] = {
    new ManagedClosable(closable)
  }

  // Custom types helpers
  type IdMapClient = String Map Client
  val IdMapClient: IdMapClient = Map.empty

  type StockKey = (Stock, Int, Int)

  type Stack = StockKey Map mutable.Queue[Order]

  type MyStockExchange = StockExchange with Load with Save with Buy with Sell with Credit with Debit with CheckSumm
}
