package ru.fan.stockexchange.model

import ru.fan.stockexchange.StockKey

object StockKey {
  // Builder for StockKey - main matching criterion triplet (stock, price, amount)
  def apply(order: Order): StockKey = (order.stock, order.price, order.amount)
}
