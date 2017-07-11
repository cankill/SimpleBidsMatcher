package ru.fan.bidmatcher.model

case class StockKey(stock: Stock, price: Int, amount: Int)

object StockKey {
  def apply(order: Order): StockKey = StockKey(order.stock, order.price, order.amount)
}
