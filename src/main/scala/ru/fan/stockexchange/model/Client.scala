package ru.fan.stockexchange.model

sealed trait Stock
case object A extends Stock
case object B extends Stock
case object C extends Stock
case object D extends Stock


case class Client(id: String, balance: Int, stocks: Stock Map Int) {
  def asString: String = {
    val stocksS = stocks.map(s => (s._1.getClass.getName, s._2)).toSeq.sortBy(_._1).map(_._2).mkString("\t")
    s"$id\t$balance\t$stocksS"
  }
}

object Client {
  def apply(id: String, balance: String, a: String, b: String, c: String, d: String): Client = {
    Client(id, balance.toInt, Map(A -> a.toInt, B -> b.toInt, C -> c.toInt, D -> d.toInt))
  }
}
