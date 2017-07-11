package ru.fan.bidmatcher.services.stockexchange

import ru.fan.bidmatcher.{IdMapClient, Stack}
import ru.fan.bidmatcher.model._

import scala.collection.mutable.Queue

abstract class StockExchange extends ClientsStore {
//  private[this] var clientsMap: IdMapClient = Map.empty
  private[this] var sellStack: Stack = Map.empty
  private[this] var buyStack: Stack = Map.empty

  def sell: (String, Stock, Int) => Unit
  def buy: (String, Stock, Int) => Unit
  def debit: (String, Int) => Unit
  def credit: (String, Int) => Unit

  def process(orders: Stream[Order]): Unit = {
    // Orders should process synchronously in order of incoming
    orders.foreach {
      case order if order.typ == SellType =>
        matchOrders(buyStack, order) match {
          case Some((order1, order2, newBuy)) =>
            exchange(order1, order2)
            
            buyStack = newBuy

          case None =>
            sellStack = placeOrder(sellStack, order)
        }

      case order if order.typ == BuyType =>
        matchOrders(sellStack, order) match {
          case Some((order1, order2, newSell)) =>
            exchange(order1, order2)
            sellStack = newSell

          case None =>
            buyStack = placeOrder(buyStack, order)
        }
    }
  }

  def matchOrders(stack: Stack, order: Order): Option[(Order, Order, Stack)] = {
    val stockKey = StockKey(order)
    stack.get(stockKey) match {
      case Some(queue) if queue.nonEmpty =>
        queue.dequeueFirst(_.clientId != order.clientId) match {
          case Some(matchOrder) =>
            val newStack = if(queue.isEmpty) stack - stockKey else stack
            Some(order, matchOrder, newStack)

          case None =>
            None
        }

      case None =>
        None
    }
  }

  def placeOrder(stack: Stack, order: Order): Stack = {
    val stockKey = StockKey(order)
    val newQueue: Queue[Order] = stack.getOrElse(stockKey, Queue.empty)
    newQueue.enqueue(order)

    stack + (stockKey -> newQueue)
  }

  def exchange(order1: Order, order2: Order): Unit = {
    applyClientOrder(order1)
    applyClientOrder(order2)
  }

  def applyClientOrder(order: Order): Unit = {
    val (stockOp, balanceOp) = order.typ match {
      case SellType => (sell, credit)
      case BuyType => (buy, debit)
    }

    stockOp(order.clientId, order.stock, order.amount)
    balanceOp(order.clientId, order.price * order.amount)
  }
}
