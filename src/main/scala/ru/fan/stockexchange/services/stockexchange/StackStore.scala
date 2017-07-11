package ru.fan.stockexchange.services.stockexchange

import ru.fan.stockexchange.Stack
import ru.fan.stockexchange.model.Stock

// Exchange Stacks
trait StackStore {
  // Stack where Sell Orders go if no match
  protected[this] var sellStack: Stack = Map.empty
  // Stack where Buy Orders go if no match
  protected[this] var buyStack: Stack = Map.empty

  protected[this] def sell: (String, Stock, Int) => Unit
  protected[this] def buy: (String, Stock, Int) => Unit
}
