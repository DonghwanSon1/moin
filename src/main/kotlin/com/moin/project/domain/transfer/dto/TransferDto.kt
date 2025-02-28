package com.moin.project.domain.transfer.dto

import com.moin.project.domain.transfer.enums.CurrencyInfo
import com.moin.project.domain.transfer.rqrs.QuoteRq
import com.moin.project.domain.user.User
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

data class TransferDto(

  @Schema(description = "송금하는 유저")
    val user: User,

  @Schema(description = "송금할 금액")
    val sourceAmount: BigDecimal,

  @Schema(description = "송금할 나라 (통화)")
    val targetCurrency: CurrencyInfo,

  @Schema(description = "환율")
    val exchangeRate: Double,

  @Schema(description = "수수료")
    val fee: BigDecimal,

  @Schema(description = "받을 금액")
    val targetAmount: BigDecimal,

  @Schema(description = "견적서 유효 시간")
    val expireTime: LocalDateTime,

  @Schema(description = "달러 환율")
    val usdExchangeRate: Double,

  @Schema(description = "달러로 받을 금액")
    val usdAmount: BigDecimal,

  @Schema(description = "송금 요청한 시간")
    val requestedDate: LocalDateTime? = null,

  @Schema(description = "송금 요청 여부")
    val transferRequest: Boolean = false

) {

  companion object {
    fun createTransferDto(rq: QuoteRq, user: User, transferCalculateDto: TransferCalculateDto): TransferDto {
      return TransferDto(
        user = user,
        sourceAmount = rq.amount.setScale(Currency.getInstance(CurrencyInfo.KRW.name).defaultFractionDigits, RoundingMode.HALF_UP).stripTrailingZeros(),
        targetCurrency = rq.targetCurrency,
        exchangeRate = transferCalculateDto.exchangeRate,
        fee = transferCalculateDto.fee,
        targetAmount = transferCalculateDto.targetAmount,
        expireTime = LocalDateTime.now(ZoneId.of("Asia/Seoul")).plusMinutes(10),
        usdExchangeRate = transferCalculateDto.usdExchangeRate,
        usdAmount = transferCalculateDto.usdAmount
      )
    }
  }
}
