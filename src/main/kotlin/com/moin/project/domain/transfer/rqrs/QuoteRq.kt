package com.moin.project.domain.transfer.rqrs

import com.moin.project.domain.transfer.enums.CurrencyInfo
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import java.math.BigDecimal

data class QuoteRq(

    @Schema(description = "송금할 원화")
    val amount: BigDecimal,

    @field:Valid
    @Schema(description = "송금할 국가")
    val targetCurrency: CurrencyInfo,

    ) {
}
