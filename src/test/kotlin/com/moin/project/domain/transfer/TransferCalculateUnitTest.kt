package com.moin.project.domain.transfer

import com.moin.project.domain.externalApi.ExternalApiService
import com.moin.project.domain.externalApi.dto.ExternalApiDto
import com.moin.project.domain.transfer.component.TransferCalculate
import com.moin.project.domain.transfer.dto.ExchangeRateDto
import com.moin.project.domain.transfer.enums.CurrencyInfo
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.junit.jupiter.api.Test

import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*

class TransferCalculateUnitTest {

  private val externalApiService: ExternalApiService = mockk<ExternalApiService>()

  private val transferCalculate: TransferCalculate = TransferCalculate(
    externalApiService
  )

  @Test
  fun `수수료 계산 - USD(100만원 이하)`() {
    // given
    val amount: BigDecimal = BigDecimal(10_000)
    val currency: CurrencyInfo = CurrencyInfo.USD

    // when
    val result: BigDecimal = transferCalculate.transferFeeCalculate(amount, currency)

    // then
    assertThat(result).isEqualTo(BigDecimal(1_020))
  }

  @Test
  fun `수수료 계산 - USD(100만원 초과)`() {
    // given
    val amount: BigDecimal = BigDecimal(1_000_001)
    val currency: CurrencyInfo = CurrencyInfo.USD

    // when
    val result: BigDecimal = transferCalculate.transferFeeCalculate(amount, currency)

    // then
    assertThat(result).isEqualTo(BigDecimal(4_000))
  }

  @Test
  fun `수수료 계산 - JPY`() {
    // given
    val highAmount: BigDecimal = BigDecimal(1_000_000)
    val lowAmount: BigDecimal = BigDecimal(10_000)
    val currency: CurrencyInfo = CurrencyInfo.JPY

    // when
    val jpyHighFee: BigDecimal = transferCalculate.transferFeeCalculate(highAmount, currency)
    val jpyLowFee: BigDecimal = transferCalculate.transferFeeCalculate(lowAmount, currency)

    // then
    assertThat(jpyHighFee).isEqualTo(BigDecimal(8_000))
    assertThat(jpyLowFee).isEqualTo(BigDecimal(3_050))
  }

  @Test
  fun `송금될 금액 계산 - USD`() {
    // given
    val amount: BigDecimal = BigDecimal(1_000_001)
    val fee: BigDecimal = BigDecimal(4_000)
    val currency: CurrencyInfo = CurrencyInfo.USD
    val exchangeRate: BigDecimal = BigDecimal(1434.0)

    // when
    val result: BigDecimal = transferCalculate.transferTargetAmountCalculate(amount, fee, currency, exchangeRate)

    // then
    assertThat(result).isEqualTo(
      BigDecimal(694.56)
        .setScale(Currency.getInstance(currency.name).defaultFractionDigits, RoundingMode.HALF_UP)
    )
  }

  @Test
  fun `송금될 금액 계산 - JPY`() {
    // given
    val amount: BigDecimal = BigDecimal(1_000_000)
    val fee: BigDecimal = BigDecimal(4_000)
    val currency: CurrencyInfo = CurrencyInfo.JPY
    val exchangeRate: BigDecimal = BigDecimal(9.1716)

    // when
    val result: BigDecimal = transferCalculate.transferTargetAmountCalculate(amount, fee, currency, exchangeRate)

    // then
    assertThat(result).isEqualTo(
      BigDecimal(108596)
        .setScale(Currency.getInstance(currency.name).defaultFractionDigits, RoundingMode.HALF_UP)
    )
  }

  @Test
  fun `환율 계산 - USD`() {
    // given
    val currency: CurrencyInfo = CurrencyInfo.USD
    val externalApiDto: List<ExternalApiDto> = listOf(
      ExternalApiDto("FRX.KRWUSD", "USD", BigDecimal(1), BigDecimal(1434))
    )
    every { externalApiService.exchangeRateApi(currency.externalApiParam) } returns externalApiDto

    // when
    val result: ExchangeRateDto = transferCalculate.transferExchangeRateCalculate(currency)

    // then
    assertThat(result.exchangeRate).isEqualTo(BigDecimal(1434))
    assertThat(result.usdExchangeRate).isEqualTo(BigDecimal(1434))
  }

  @Test
  fun `환율 계산 - JPY`() {
    // given
    val currency: CurrencyInfo = CurrencyInfo.JPY
    val externalApiDto: List<ExternalApiDto> = listOf(
      ExternalApiDto("FRX.KRWJPY", "JPY", BigDecimal(100), BigDecimal(917.11)),
      ExternalApiDto("FRX.KRWUSD", "USD", BigDecimal(1), BigDecimal(1434))
    )
    every { externalApiService.exchangeRateApi(currency.externalApiParam) } returns externalApiDto

    // when
    val result: ExchangeRateDto = transferCalculate.transferExchangeRateCalculate(currency)

    // then
    assertThat(result.exchangeRate).isEqualTo(
      BigDecimal(9.1711)
        .setScale(12, RoundingMode.HALF_UP).stripTrailingZeros()
    )
    assertThat(result.usdExchangeRate).isEqualTo(BigDecimal(1434))
  }

  @Test
  fun `오늘 송금할 USD 금액 계산`() {
    // given
    val usdAmount: BigDecimal = BigDecimal(694.56)
      .setScale(Currency.getInstance("USD").defaultFractionDigits, RoundingMode.HALF_UP)
    val totalUsdAmount: BigDecimal = BigDecimal(486.30)
      .setScale(Currency.getInstance("USD").defaultFractionDigits, RoundingMode.HALF_UP)

    // when
    val result: BigDecimal = transferCalculate.todayTotalAmountCalculate(usdAmount, totalUsdAmount)

    // then
    assertThat(result).isEqualTo(
      BigDecimal(1180.86)
        .setScale(Currency.getInstance("USD").defaultFractionDigits, RoundingMode.HALF_UP)
    )
  }
}