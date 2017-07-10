package ru.fan.bidmatcher

import java.nio.file.{Files, Path}

import com.typesafe.scalalogging.LazyLogging
import ru.fan.bidmatcher.configure.CmdLineOptions
import ru.fan.bidmatcher.model._

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
      using(Files.newBufferedReader(config.clients)) { reader =>
        val indicies = Stream from 1
        val clientsLines: Stream[String] = reader
        val clientsLinesWithIndicies = clientsLines zip indicies

        val clients = clientsLinesWithIndicies.map { case (clientLine, i) =>
          clientLine.split("\t") match {
            case Array(id, balance, a, b, c, d) =>
              Client(id, balance, a, b, c, d)

            case _ =>
              throw new IllegalArgumentException(s"Wrong format data in file ${config.clients} at line $i [$clientLine]")
          }
        }

        logger.debug(clients.toString())

//
//        for {
//          clientLine <- clientsLines
//          i <- indicies
//        } yield {
//          val vals = clientLine.split("\t")
//        }
//
//
//        val vals = reader.readLine.split("\t")
//        println(vals)
      }

//      val clients = config.clients
//
//      if(clients.canRead) {
//
//
//        val accounts  = Source.fromFile(clients).foreach(print)
//      }

    case None =>
    // arguments are bad, error message will have been displayed
  }
}
