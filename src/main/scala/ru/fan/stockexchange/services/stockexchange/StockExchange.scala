package ru.fan.stockexchange.services.stockexchange

import ru.fan.stockexchange.{MyStockExchange, Stack}
import ru.fan.stockexchange.model._

import scala.collection.mutable

abstract class StockExchange extends ClientsStore with StackStore {
  /**
    * Main orders processor. It takes a Stream os Orders and process them one-by-one
    * with order respect, matching orders by the same stock, amount and price.
    * Result is a change of inner Buy/Sell stacks of StackStore and Clients balances in ClientsStore
    * Attention: Not Thread-safe implementation.
    * @param orders Lazy Stream of Orders from external file
    */
  def process(orders: Stream[Order]): Unit = {
    // Orders should process synchronously in order of incoming
    orders.foreach {
      case sellOrder if sellOrder.typ == SellType =>
        // Looking for matching order in a buyStack
        matchOrders(buyStack, sellOrder) match {
          case Some((order1, order2, newBuy)) =>
            // Apply matching orders
            applyClientsOrder(order1, order2)

            // Rewrite buyStack, because one order was removed from stack
            buyStack = newBuy

          case None =>
            // If no matching Order found - place Sell Order in a sellStack
            sellStack = placeOrder(sellStack, sellOrder)
        }

      case buyOrder if buyOrder.typ == BuyType =>
        // Looking for matching order in a sellStack
        matchOrders(sellStack, buyOrder) match {
          case Some((order1, order2, newSell)) =>
            // Apply matching orders
            applyClientsOrder(order1, order2)

            // Rewrite sellStack, because one order was removed from stack
            sellStack = newSell

          case None =>
            // If no matching Order found - place Buy Order in a buyStack
            buyStack = placeOrder(buyStack, buyOrder)
        }
    }
  }

  /**
    * Match finder. Will look order with te same stock, amount, price in a provided stack.
    * Used mutable Queue in order to support ignorance of an Orders for same Client.
    * @param stack Provided stack to look for matching Order in
    * @param order Order to find and match
    * @return Option of triplet with two matching Orders and updated stack
    */
  private[this] def matchOrders(stack: Stack, order: Order): Option[(Order, Order, Stack)] = {
    // Main matching criterion triplet (stock, price, amount)
    val stockKey = StockKey(order)
    stack.get(stockKey) match {
      case Some(queue) if queue.nonEmpty =>
        // Ignore orders for the same Client. No match if clientId is the same
        queue.dequeueFirst(_.clientId != order.clientId) match {
          case Some(matchOrder) =>
            // drop empty Queue from stack
            val newStack = if(queue.isEmpty) stack - stockKey else stack
            Some(order, matchOrder, newStack)

          case None =>
            None
        }

      case None =>
        None
    }
  }

  /**
    * Place non matched Order into provided stack.
    * Used Queue to remember an order of incoming.
    * First-in Orders has priority to be matched.
    * @param stack Provided Stack to store an Order
    * @param order An Order to store
    * @return New updated Stack
    */
  private[this] def placeOrder(stack: Stack, order: Order): Stack = {
    val stockKey = StockKey(order)
    val newQueue: mutable.Queue[Order] = stack.getOrElse(stockKey, mutable.Queue.empty)
    newQueue.enqueue(order)

    stack + (stockKey -> newQueue)
  }

  /**
    * Apply matched Orders for Clients
    * @param orders List of Orders to apply
    */
  private[this] def applyClientsOrder(orders: Order*): Unit = {
    orders.foreach(applyClientOrder)
  }

  /**
    * Apply Order for Client.
    * For Sell Order we must remove provided amount of a stocks from Client stock balance and credit Client money balance for stock amount * price
    * For Buy Order we must add provided amount of a stocks to Client stock balance and debit Client money balance for stock amount * price
    * @param order Order to apply
    */
  private[this] def applyClientOrder(order: Order): Unit = {
    val (stockOp, balanceOp) = order.typ match {
      case SellType => (sell, credit)
      case BuyType => (buy, debit)
    }

    // Apply stock balance changes
    stockOp(order.clientId, order.stock, order.amount)
    // Apply money balance changes
    balanceOp(order.clientId, order.price * order.amount)
  }
}