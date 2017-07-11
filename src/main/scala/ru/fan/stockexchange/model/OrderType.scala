package ru.fan.stockexchange.model

sealed trait OrderType
case object SellType extends OrderType
case object BuyType extends OrderType