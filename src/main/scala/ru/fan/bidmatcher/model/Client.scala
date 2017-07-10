package ru.fan.bidmatcher.model

sealed trait Stock
case object A extends Stock
case object B extends Stock
case object C extends Stock
case object D extends Stock


case class Client(id: String, balance: Int, stoks: Stock Map Int)

object Client {
  def apply(id: String, balance: String, a: String, b: String, c: String, d: String): Client = {
    Client(id, balance.toInt, Map(A -> a.toInt, B -> b.toInt, C -> c.toInt, D -> d.toInt))
  }
}
