package com.moin.project.domain.transfer.rqrs

import com.fasterxml.jackson.annotation.JsonFormat
import com.moin.project.domain.transfer.Quotation
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDateTime

data class QuoteRs(

    @Schema(description = "견적서 ID")
    val quoteId: Long? = null,

    @Schema(description = "환율")
    val exchangeRate: Double? = null,

    @Schema(description = "견적서 유효 날짜/시간")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val expireTime: LocalDateTime? = null,

    @Schema(description = "송금 받을 돈")
    val targetAmount: BigDecimal? = null,

) {
    companion object {
        fun createQuoteRs(quotation: Quotation): QuoteRs{
            return QuoteRs(
                quoteId = quotation.id,
                exchangeRate = quotation.exchangeRate,
                expireTime = quotation.expireTime,
                targetAmount = quotation.targetAmount
            )
        }
    }
}
