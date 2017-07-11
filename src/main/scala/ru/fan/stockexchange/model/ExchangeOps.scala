package ru.fan.stockexchange.model

import com.typesafe.scalalogging.LazyLogging
import ru.fan.stockexchange.IdMapClient

// Generic Balance Operations definitions for exchange
trait BalanceOp extends LazyLogging { this: ClientsStore =>
  /**
    * Generic money balance operation.
    * Modify ClientsStore.
    * @param id Client identity
    * @param amount Amount of money to operate
    * @param op Abstract operation on balance applying amount
    */
  protected[this] def operate(id: String, amount: Int)(op: (Int, Int) => Int): Unit = {
    clientsMap = clientsMap.get(id) match {
      case Some(client) =>
        val newClient = client.copy(balance = op(client.balance, amount))
        clientsMap + (id -> newClient)

      case None =>
        logger.error(s"Client with id: $id not Found")
        clientsMap
    }
  }
}

// Adds Debit Client money balance
trait Debit extends BalanceOp { this: ClientsStore =>
  def debit: (String, Int) => Unit = (id, amount) => operate(id, amount)(_ - _)
}

// Adds Credit Client money balance
trait Credit extends BalanceOp { this: ClientsStore =>
  def credit: (String, Int) => Unit = (id, amount) => operate(id, amount)(_ + _)
}

// Generic Stock Balance Operations definitions for exchange
trait ExchangeOp extends LazyLogging { this: ClientsStore =>
  /**
    * Generic stocks balance operation.
    * @param id Client identity
    * @param stock Stock name to change balance of
    * @param amount Stocks amount to apply
    * @param op Abstract operation on Stock balance applying amount
    */
  protected[this] def exchange(id: String, stock: Stock, amount: Int)(op: (Int, Int) => Int): Unit = {
    clientsMap = clientsMap.get(id) match {
      case Some(client) =>
        val stockCount = client.stocks.getOrElse(stock, 0)
        val newStocks = client.stocks + (stock -> op(stockCount, amount))

        val newClient = client.copy(stocks = newStocks)
        clientsMap + (id -> newClient)

      case None =>
        logger.error(s"Client with id: $id not Found")
        clientsMap
    }
  }
}

// Adds Sell Stock operation
trait Sell extends ExchangeOp { this: ClientsStore =>
  def sell: (String, Stock, Int) => Unit = (id, stock, amount) => exchange(id, stock, amount)(_ - _)
}

// Adds Buy Stock operation
trait Buy extends ExchangeOp { this: ClientsStore =>
  def buy: (String, Stock, Int) => Unit = (id, stock, amount) => exchange(id, stock, amount)(_ + _)
}

// Adds Load Clients list from external source support
trait Load extends LazyLogging { this: ClientsStore =>
  def load(clientsList: Stream[Client]): Unit = {
    clientsMap = (IdMapClient /: clientsList) {
      case (m, client) if !m.contains(client.id) =>
        logger.debug(s"Loaded client with ID: ${client.id}")
        m + (client.id -> client)

      case (_, client) =>
        throw new IllegalStateException(s"Client with id: ${client.id} is already added")
    }
  }
}

// Returns a Clients balances for external store
trait Save { this: ClientsStore =>
  def save: Stream[Client] = clientsMap.values.toStream
}

// Adds checksum support.
// Sum of clients money balances and Sum of a Stocks count per Stock must be constant.
// If one Client buy 10 stocks other client sell them
// If one Client receive a money for selling Stocks, the same amount of money will be withdraw from Client balance who buy.
trait CheckSumm { this: ClientsStore =>
  def checkSumm: (Int, Int, Int, Int, Int) = {
    ((0, 0, 0, 0, 0) /: clientsMap) {
      case ((balance, a, b, c, d), (_, client)) =>
        val aN = client.stocks.getOrElse(A, 0)
        val bN = client.stocks.getOrElse(B, 0)
        val cN = client.stocks.getOrElse(C, 0)
        val dN = client.stocks.getOrElse(D, 0)
        (balance + client.balance, a + aN, b + bN, c + cN, d + dN)
    }
  }
}

