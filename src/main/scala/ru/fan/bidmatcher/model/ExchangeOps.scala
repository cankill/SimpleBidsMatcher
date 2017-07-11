package ru.fan.bidmatcher.model

import com.typesafe.scalalogging.LazyLogging
import ru.fan.bidmatcher.IdMapClient

trait ClientsStore {
  var clientsMap: IdMapClient = Map.empty
}

trait BalanceOp extends LazyLogging { this: ClientsStore =>
  def operate(id: String, amount: Int)(op: (Int, Int) => Int): Unit = {
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

trait Debit extends BalanceOp { this: ClientsStore =>
  def debit: (String, Int) => Unit = (id, amount) => operate(id, amount)(_ - _)
}

trait Credit extends BalanceOp { this: ClientsStore =>
  def credit: (String, Int) => Unit = (id, amount) => operate(id, amount)(_ + _)
}

trait ExchangeOp extends LazyLogging { this: ClientsStore =>
  def exchange(id: String, stock: Stock, amount: Int)(op: (Int, Int) => Int): Unit = {
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

trait Sell extends ExchangeOp { this: ClientsStore =>
  def sell: (String, Stock, Int) => Unit = (id, stock, amount) => exchange(id, stock, amount)(_ - _)
}

trait Buy extends ExchangeOp { this: ClientsStore =>
  def buy: (String, Stock, Int) => Unit = (id, stock, amount) => exchange(id, stock, amount)(_ + _)
}

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

trait Save { this: ClientsStore =>
  def save: Stream[Client] = clientsMap.values.toStream
}

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

