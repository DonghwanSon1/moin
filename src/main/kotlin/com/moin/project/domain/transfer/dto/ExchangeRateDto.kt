package com.moin.project.domain.transfer.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal

data class ExchangeRateDto(

  @Schema(description = "미국 환율")
  val usdExchangeRate: BigDecimal,

  @Schema(description = "송금할 나라의 환율")
  val exchangeRate: BigDecimal,

)
