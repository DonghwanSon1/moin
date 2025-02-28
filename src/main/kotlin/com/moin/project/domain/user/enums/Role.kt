package com.moin.project.domain.user.enums

import java.math.BigDecimal

enum class Role(val desc: String, val limitAmount: BigDecimal) {
  REG_NO("개인회원", BigDecimal(1000)),
  BUSINESS_NO("법인회원", BigDecimal(5000))
}