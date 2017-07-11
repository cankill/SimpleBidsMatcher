package ru.fan.stockexchange.services.parser

import java.io.BufferedReader

import ru.fan.stockexchange.model.{Client, Order}

import scala.util.{Failure, Success, Try}

object FilesParser {
  /**
    * Implicit adapter for java BufferedReader into scala Stream[String]
    * @param bufferedReader - java BufferedReader of open file
    * @return
    */
  implicit def toScalaStream(bufferedReader: BufferedReader): Stream[String] = {
    if(bufferedReader.ready()) bufferedReader.readLine() #:: toScalaStream(bufferedReader)
    else Stream.empty
  }

  /**
    * Common Parser for files with lines delimited by "\n" and with "\t" as fields delimiter
    * TODO: No comments alowed, no empty lines allowed
    * @param reader Java BufferedReader of open file
    * @param filename Name for open file to use in exception
    * @param builder Implicit builder to build T object from parsed Array of params.
    *                Takes Array[String] as input and object of T as output
    * @tparam T Type for output objects in Stream
    * @return
    */
  def parse[T](reader: BufferedReader, filename: String)(implicit builder: (Array[String]) => T): Stream[T] = {
    val indicies = Stream from 1
    val lines: Stream[String] = reader
    // We need a line number in file for pretty errors messages
    val linesWithIndicies = lines zip indicies

    linesWithIndicies.map { case (line, i) =>
      Try(builder(line.split("\t"))) match {
        case Failure(exception) =>
          throw new IllegalArgumentException(s"Wrong format data in file $filename at line $i [$line]", exception)

        case Success(item) =>
          item
      }
    }
  }

  /**
    * Implicit builder for Order from array of params
    * @param params Array of params
    * @return
    */
  implicit def orderBuilder(params: Array[String]): Order = params match {
    case Array(id, t, stock, price, amount) => Order(id, t, stock, price, amount)
    case _ => throw new IllegalArgumentException(s"Could not construct Order from params: $params")
  }

  /**
    * Implicit builder for Client from array of params
    * @param params Array of params
    * @return
    */
  implicit def clientBuilder(params: Array[String]): Client = params match {
    case Array(id, balance, a, b, c, d) => Client(id, balance, a, b, c, d)
    case _ => throw new IllegalArgumentException(s"Could not construct Client from params: $params")
  }
}
