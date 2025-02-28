package com.moin.project.domain.transfer.rqrs

import com.moin.project.domain.transfer.enums.CurrencyInfo
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import java.math.BigDecimal

data class TransferRequestRq(

    @Schema(description = "송금 견적서 ID")
    val quoteId: Long,

) {
}
