package com.moin.project.domain.transfer.enums


enum class CurrencyInfo(val desc: String, val externalApiParam: String) {
  USD("달러", "FRX.KRWUSD"),
  JPY("엔화", "FRX.KRWJPY"),
  KRW("원화", "");
}