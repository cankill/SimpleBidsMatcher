package ru.fan.bidmatcher

import java.nio.file.{Files, OpenOption, Path}

import com.typesafe.scalalogging.LazyLogging
import ru.fan.bidmatcher.configure.CmdLineOptions
import ru.fan.bidmatcher.model._
import ru.fan.bidmatcher.services.stockexchange.StockExchange

import scala.util.{Failure, Success, Try}

object SimpleScalaMatcher extends App with LazyLogging {
  val parser = new scopt.OptionParser[CmdLineOptions]("scala -cp ./ SimpleScalaMatcher") {
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
      val exchange: MyStockExchange = new StockExchange with Load with Save with Buy with Sell with Credit with Debit with CheckSumm

      using(Files.newBufferedReader(config.clients)) { reader =>
        val indicies = Stream from 1
        val clientsLines: Stream[String] = reader
        val clientsLinesWithIndicies = clientsLines zip indicies

        val clients = clientsLinesWithIndicies.map { case (clientLine, i) =>
          clientLine.split("\t") match {
            case Array(id, balance, a, b, c, d) =>
              Try(Client(id, balance, a, b, c, d)) match {
                case Failure(exception) =>
                  throw new IllegalArgumentException(s"Wrong format data in file ${config.clients} at line $i [$clientLine]", exception)

                case Success(client) =>
                  client
              }

            case _ =>
              throw new IllegalArgumentException(s"Wrong format data in file ${config.clients} at line $i [$clientLine]")
          }
        }

        exchange.load(clients)
      }

      val checksum1 = exchange.checkSumm

      using(Files.newBufferedReader(config.orders)) { reader =>
        val indicies = Stream from 1
        val ordersLines: Stream[String] = reader
        val ordersLinesWithIndicies = ordersLines zip indicies

        val orders = ordersLinesWithIndicies.map { case (orderLine, i) =>
          orderLine.split("\t") match {
            case Array(id, typ, stock, price, amount) =>
              Try(Order(id, typ, stock, price, amount)) match {
                case Failure(exception) =>
                  throw new IllegalArgumentException(s"Wrong format data in file ${config.orders} at line $i [$orderLine]", exception)

                case Success(order) =>
                  order
              }


            case _ =>
              throw new IllegalArgumentException(s"Wrong format data in file ${config.orders} at line $i [$orderLine]")
          }
        }

        exchange.process(orders)
      }

      val checksum2 = exchange.checkSumm

      val result = exchange.save

      using(Files.newBufferedWriter(config.result)) { writer =>
        result.sortBy(_.id).foreach { client =>
          writer.write(client.asString)
          writer.newLine()
        }
      }

      logger.debug(s"Checksumms: $checksum1, $checksum2 are equals: ${checksum1 == checksum2}")

    case None =>
    // arguments are bad, error message will have been displayed
  }
}
