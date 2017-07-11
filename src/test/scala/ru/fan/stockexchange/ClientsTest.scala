package ru.fan.stockexchange

import org.scalatest.{FlatSpec, FunSuite, Matchers}
import ru.fan.stockexchange.model._

class ClientsTest extends FunSuite {
  val testClient1 = Client("C1", 1000, Map(A->10, B->20, C->30, D->40))

  test("Client creation") {
    val client: Client = Client("C1", "1000", "10", "20", "30", "40")

    assert(client === testClient1)
  }

  test("Client creation with exception") {
    val wrongBalance = "1000O"
    
    val thrown = intercept[java.lang.NumberFormatException] {
      Client("C1", wrongBalance, "10", "20", "30", "40")
    }

    assert(thrown.getMessage === s"""For input string: "$wrongBalance"""")
  }
}
