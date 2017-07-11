package ru.fan.stockexchange.model

case class Order(clientId: String, typ: OrderType, stock: Stock, price: Int, amount: Int)

object Order {
  def apply(clientId: String, typ: String, stockS: String, price: String, amount: String): Order = {
    val orderType = typ match {
      case "b" => BuyType
      case "s" => SellType
      case _ => throw new IllegalArgumentException("Order operation should be one of [b,s]")
    }

    val stock = stockS match {
      case "A" => A
      case "B" => B
      case "C" => C
      case "D" => D
      case _ => throw new IllegalArgumentException("Stock should be one of [A,B,C,D]")
    }
    
    new Order(clientId, orderType, stock, price.toInt, amount.toInt)
  }
}
