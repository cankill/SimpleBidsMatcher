package ru.fan

import java.io.{BufferedReader, Closeable}
import java.nio.file.{Path, Paths}

import scala.language.implicitConversions

package object bidmatcher {
  implicit val pathRead: scopt.Read[Path] = scopt.Read.reads {p => Paths.get(p)}
  
  trait Managed[T] {
    def onEnter(): T
    def onExit(t:Throwable = null)
    def attempt(block: => Unit) {
      try { block } finally {}
    }
  }

  def using[T <: Any, R](managed: Managed[T])(block: T => R): R = {
    val resource = managed.onEnter()
    var exception = false
    try {
      block(resource)
    } catch  {
      case t:Throwable => {
        exception = true
        managed.onExit(t)
        throw t
      }
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

  implicit def toScalaStream(bufferedReader: BufferedReader): Stream[String] = {
    if(bufferedReader.ready()) bufferedReader.readLine() #:: toScalaStream(bufferedReader)
    else Stream.empty
  }
}
