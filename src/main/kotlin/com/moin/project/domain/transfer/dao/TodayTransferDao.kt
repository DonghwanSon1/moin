package com.moin.project.domain.transfer.dao

import com.moin.project.domain.user.enums.Role
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal

data class TodayTransferDao(

  @Schema(description = "유저 ID")
  val userId: String? = null,

  @Schema(description = "유저 이름")
  val name: String? = null,

  @Schema(description = "유저 역할")
  val idType: Role? = null,

  @Schema(description = "오늘 총 송금 횟수")
  val todayTransferCount: Long? = null,

  @Schema(description = "오늘 총 송금한 USD")
  val todayTransferUsdAmount: BigDecimal? = null,

  ) {

}
