package ru.fan.stockexchange.model

import ru.fan.stockexchange.IdMapClient

trait ClientsStore {
  protected[this] var clientsMap: IdMapClient = Map.empty

  protected[this] def debit: (String, Int) => Unit
  protected[this] def credit: (String, Int) => Unit
}