package com.moin.project.domain.transfer.dto

import com.moin.project.domain.transfer.enums.CurrencyInfo
import com.moin.project.domain.user.User
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDateTime

data class TransferCalculateDto(

  @Schema(description = "환율")
    val exchangeRate: Double,

  @Schema(description = "수수료")
    val fee: BigDecimal,

  @Schema(description = "받을 금액")
    val targetAmount: BigDecimal,

  @Schema(description = "달러 환율")
    val usdExchangeRate: Double,

  @Schema(description = "달러로 받을 금액")
    val usdAmount: BigDecimal,

) {

}
