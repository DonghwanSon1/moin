package com.moin.project.domain.transfer.rqrs

import com.moin.project.domain.transfer.dao.TransferHistoryDao
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal

data class TransferListRs(

  @Schema(description = "유저 ID")
  val userId: String? = null,

  @Schema(description = "유저 이름")
  val name: String? = null,

  @Schema(description = "오늘 총 송금 횟수")
  val todayTransferCount: Long? = null,

  @Schema(description = "오늘 총 송금한 USD")
  val todayTransferUsdAmount: BigDecimal? = null,

  @Schema(description = "총 송금 내역")
  val history: List<TransferHistoryDao>? = null,

  ) {

}
