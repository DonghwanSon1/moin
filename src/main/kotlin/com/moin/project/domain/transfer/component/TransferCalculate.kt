package com.moin.project.domain.transfer.component


import com.moin.project.domain.externalApi.ExternalApiService
import com.moin.project.domain.transfer.dto.ExchangeRateDto
import com.moin.project.domain.externalApi.dto.ExternalApiDto
import com.moin.project.domain.transfer.enums.CurrencyInfo
import com.moin.project.domain.transfer.enums.FeeInfo
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Currency

@Component
class TransferCalculate(
  private val externalApiService: ExternalApiService,

) {

  /**
   * 수수료 계산 (송금하는 금액, 송금할 나라)
   */
  fun transferFeeCalculate(amount: BigDecimal, targetCurrency: CurrencyInfo): BigDecimal {
    // 요청한 통화별 고정 수수료, 수수료율
    val (fixFee, feeRate) = FeeInfo.getFeeInfo(targetCurrency, amount)

    return (amount.multiply(feeRate.toBigDecimal())).plus(fixFee.toBigDecimal()).setScale(
      Currency.getInstance(CurrencyInfo.KRW.name).defaultFractionDigits, RoundingMode.HALF_UP)
  }

  /**
   * 송금될 금액 계산 (송금하는 금액, 수수료, 송금할 나라, 환율)
   */
  fun transferTargetAmountCalculate(amount: BigDecimal, fee: BigDecimal, targetCurrency: CurrencyInfo, exchangeRate: BigDecimal): BigDecimal {
    return (amount.minus(fee)).divide(exchangeRate,
      Currency.getInstance(targetCurrency.name).defaultFractionDigits, RoundingMode.HALF_UP)
  }

  /**
   * 환율 계산 (송금할 나라)
   * - 환율 정보를 외부 API 에서 가져와 환율 계산한다.
   */
  fun transferExchangeRateCalculate(targetCurrency: CurrencyInfo): ExchangeRateDto {
    val externalApiDto: List<ExternalApiDto> = externalApiService.exchangeRateApi(targetCurrency.externalApiParam)

    val (exchangeRate, usdExchangeRate) = externalApiDto.associateBy { it.currencyCode }.let { map ->
      val exchangeRate = map[targetCurrency.name].let {
        dto -> dto!!.basePrice.divide(dto.currencyUnit, 12, RoundingMode.HALF_UP).stripTrailingZeros()
      }
      val usdExchangeRate = map[CurrencyInfo.USD.name].let {
        dto -> dto!!.basePrice.divide(dto.currencyUnit, 12, RoundingMode.HALF_UP).stripTrailingZeros()
      }
      exchangeRate to usdExchangeRate
    }
    return ExchangeRateDto(exchangeRate = exchangeRate, usdExchangeRate = usdExchangeRate)
  }

  /**
   * 오늘 송금할 USD 금액 계산 (오늘 송금할 금액, 오늘 송금한 총액)!
   */
  fun todayTotalAmountCalculate(usdAmount: BigDecimal, totalUsdAmount: BigDecimal): BigDecimal {
    return totalUsdAmount.plus(usdAmount)
  }
}
