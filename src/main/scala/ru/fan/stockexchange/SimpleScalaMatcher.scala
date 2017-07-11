package ru.fan.stockexchange

import java.nio.file.{Files, OpenOption, Path}

import com.typesafe.scalalogging.LazyLogging
import ru.fan.stockexchange.configure.CmdLineOptions
import ru.fan.stockexchange.model._
import ru.fan.stockexchange.services.parser.FilesParser
import ru.fan.stockexchange.services.parser.FilesParser.{clientBuilder, orderBuilder}
import ru.fan.stockexchange.services.stockexchange.StockExchange

object SimpleScalaMatcher extends App with LazyLogging {
  val parser = new scopt.OptionParser[CmdLineOptions]("java -jar pathToJar/simplestackexchange_2.12-1.0.jar") {
    head("SimpleScalaMatcher", "1.0")

    opt[Path]('c', "clients").required().valueName("<file>").
      action( (x, c) => c.copy(clients = x) ).
      text("clients is an required input file with clients accounts")

    opt[Path]('r', "result").optional().valueName("<file>").
      action( (x, c) => c.copy(result = x) ).
      text("result is an optional output file for storing results accounts state of orders matching (default: result.txt)")

    opt[Path]('o', "orders").required().valueName("<file>").
      action( (x, c) => c.copy(orders = x) ).
      text("orders is a required input file with orders")
  }

  parser.parse(args, CmdLineOptions()) match {
    case Some(config) =>
      // Construct exchange with all possible functionality
      val exchange: MyStockExchange = new StockExchange with Load with Save with Buy with Sell with Credit with Debit with CheckSumm

      // Ensuring resource file with clients is closed after use
      using(Files.newBufferedReader(config.clients)) { reader =>
        // FileParser will make a Stream of Clients
        val clients = FilesParser.parse[Client](reader, config.clients.toString)
        exchange.load(clients)

        // We'll save initial checksum (on Exchange the amount of all client's balances and amount of all stocks is a constant)
        val checksum1 = exchange.checkSumm

        // Ensure resource file with orders is closed after use
        using(Files.newBufferedReader(config.orders)) { reader =>
          // FileParser will make a Stream of Orders
          val orders = FilesParser.parse[Order](reader, config.orders.toString)
          exchange.process(orders)

          // We'll save checksum after orders processing it should remain constant
          val checksum2 = exchange.checkSumm

          val result = exchange.save

          // Ensure resource file with results is closed after use
          using(Files.newBufferedWriter(config.result)) { writer =>
            result.sortBy(_.id).foreach { client =>
              writer.write(client.asString)
              writer.newLine()
            }
          }

          logger.debug(s"Checksumms: $checksum1, $checksum2 are equals: ${checksum1 == checksum2}")
        }
      }

    case None =>
    // arguments are bad, error message will have been displayed
  }
}
