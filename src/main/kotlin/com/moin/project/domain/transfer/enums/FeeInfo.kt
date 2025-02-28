package com.moin.project.domain.transfer.enums

import com.moin.project.common.exception.CommonException
import com.moin.project.common.exception.CommonExceptionCode
import java.math.BigDecimal

enum class FeeInfo(val fixFee: Int, val feeRate: Double) {
  USD_1(1000, 0.002),
  USD_100(3000, 0.001),
  JPY(3000, 0.005);

  companion object {
    fun getFeeInfo(targetCurrencyInfo: CurrencyInfo, amount: BigDecimal): Pair<Int, Double> {
      return when (targetCurrencyInfo) {
        CurrencyInfo.USD -> {
          if (amount <= BigDecimal(1000000)) {
            USD_1.fixFee to USD_1.feeRate
          } else {
            USD_100.fixFee to USD_100.feeRate
          }
        }
        CurrencyInfo.JPY -> JPY.fixFee to JPY.feeRate
        else -> throw CommonException(CommonExceptionCode.BAD_REQUEST)
      }
    }
  }
}