package com.moin.project.domain.transfer.dao

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.moin.project.domain.transfer.enums.CurrencyInfo
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime
import java.util.*

@JsonPropertyOrder(
  "sourceAmount",
  "fee",
  "usdExchangeRate",
  "usdAmount",
  "targetCurrency",
  "exchangeRate",
  "targetAmount",
  "requestedDate"
)
data class TransferHistoryDao(

  @Schema(description = "송금할 금액")
  private val _sourceAmount: BigDecimal? = null,

  @Schema(description = "수수료")
  private val _fee: BigDecimal? = null,

  @Schema(description = "달러 환율")
  val usdExchangeRate: Double? = null,

  @Schema(description = "달러 금액")
  val usdAmount: BigDecimal? = null,

  @Schema(description = "송금할 나라 (통화)")
  val targetCurrency: CurrencyInfo? = null,

  @Schema(description = "환율")
  val exchangeRate: Double? = null,

  @Schema(description = "받을 금액")
  private val _targetAmount: BigDecimal? = null,

  @Schema(description = "송금 요청한 시간")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  val requestedDate: LocalDateTime? = null,

  ) {

  val sourceAmount: BigDecimal?
    get() = _sourceAmount?.setScale(
      Currency.getInstance(CurrencyInfo.KRW.name).defaultFractionDigits, RoundingMode.HALF_UP)

  val fee: BigDecimal?
    get() = _fee?.setScale(
      Currency.getInstance(CurrencyInfo.KRW.name).defaultFractionDigits, RoundingMode.HALF_UP)

  val targetAmount: BigDecimal?
    get() = _targetAmount?.setScale(
      Currency.getInstance(targetCurrency!!.name).defaultFractionDigits, RoundingMode.HALF_UP)

}
