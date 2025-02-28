package com.moin.project.domain.externalApi.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal

data class ExternalApiDto(

  @Schema(description = "Code")
  val code: String,

  @Schema(description = "통화 코드")
  val currencyCode: String,

  @Schema(description = "달러/엔화 기준값")
  val currencyUnit: BigDecimal,

  @Schema(description = "달러/엔화의 기준값에 대한 원화 가격")
  val basePrice: BigDecimal
)
