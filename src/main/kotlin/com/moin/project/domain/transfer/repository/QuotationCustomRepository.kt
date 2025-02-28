package com.moin.project.domain.transfer.repository

import com.moin.project.domain.transfer.dao.TodayTransferDao
import com.moin.project.domain.transfer.dao.TransferHistoryDao


interface QuotationCustomRepository {
  fun searchUserIdTypeAndTodayUsdAmount(userSn: Long): TodayTransferDao
  fun searchTransferHistory(userSn: Long): List<TransferHistoryDao>?
}