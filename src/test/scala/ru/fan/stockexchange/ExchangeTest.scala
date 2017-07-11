package ru.fan.stockexchange

import org.scalatest.FunSuite
import ru.fan.stockexchange.model._
import ru.fan.stockexchange.services.stockexchange.StockExchange

class ExchangeTest extends FunSuite {
  val testClient1 = Client("C1", 1000, Map(A->10, B->20, C->30, D->40))
  val testClient2 = Client("C2", 1000, Map(A->50, B->60, C->70, D->80))
  val clients: Stream[Client] = testClient1 #:: testClient2 #:: Stream.empty
  val clientsE: Stream[Client] = testClient1 #:: testClient2 #:: testClient1 #:: Stream.empty

  val order1 = Order("C1", SellType, A, 10, 10)
  val order2 = Order("C2", BuyType, A, 10, 10)
  val order3 = Order("C1", BuyType, A, 10, 10)
  val orders: Stream[Order] = order1 #:: order2 #:: Stream.empty
  val orders2: Stream[Order] = order1 #:: order3 #:: Stream.empty
  val orders3: Stream[Order] = order1 #:: order3 #:: order2 #:: Stream.empty

  val testClient1m = Client("C1", 1100, Map(A->0, B->20, C->30, D->40))
  val testClient2m = Client("C2", 900, Map(A->60, B->60, C->70, D->80))
  val clientsm: Stream[Client] = testClient1m #:: testClient2m #:: Stream.empty

  test("Exchange creation") {
    val exchange: StockExchange with Load with Save = new StockExchange with Load with Save {
      override def sell: (String, Stock, Int) => Unit = ???
      override def buy: (String, Stock, Int) => Unit = ???
      override def debit: (String, Int) => Unit = ???
      override def credit: (String, Int) => Unit = ???
    }

    exchange.load(clients)

    val saved = exchange.save

    assert(saved === clients)
  }

  test("Exchange clients creation exception") {
    val exchange: StockExchange with Load with Save = new StockExchange with Load with Save {
      override def sell: (String, Stock, Int) => Unit = ???
      override def buy: (String, Stock, Int) => Unit = ???
      override def debit: (String, Int) => Unit = ???
      override def credit: (String, Int) => Unit = ???
    }

    val thrown = intercept[IllegalStateException] {
      exchange.load(clientsE)
    }

    assert(thrown.getMessage === s"""Client with id: C1 is already added""")
  }

  test("Exchange load/process") {
    val exchange: StockExchange with Load with Save with CheckSumm = new StockExchange with Load with Save with Buy with Sell with Credit with Debit with CheckSumm

    exchange.load(clients)

    val checksum1 = exchange.checkSumm

    exchange.process(orders)

    val checksum2 = exchange.checkSumm

    val saved = exchange.save

    assert(saved === clientsm)
    assert(checksum1 === checksum2)
  }

  test("Exchange process ignore buy/sell to self") {
    val exchange: StockExchange with Load with Save with CheckSumm = new StockExchange with Load with Save with Buy with Sell with Credit with Debit with CheckSumm

    exchange.load(clients)

    val checksum1 = exchange.checkSumm

    exchange.process(orders3)

    val checksum2 = exchange.checkSumm

    val saved = exchange.save

    assert(saved === clientsm)
    assert(checksum1 === checksum2)
  }
}
