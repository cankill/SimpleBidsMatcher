package ru.fan.stockexchange.configure

import java.io.File
import java.nio.file.{Path, Paths}

case class CmdLineOptions(clients: Path = Paths.get("clients.txt"), result: Path = Paths.get("result.txt"), orders: Path = Paths.get("orders.txt"))
