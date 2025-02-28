package com.moin.project.domain.transfer.component


import com.moin.project.common.exception.CommonException
import com.moin.project.common.exception.CommonExceptionCode
import com.moin.project.domain.transfer.dao.TodayTransferDao
import com.moin.project.domain.transfer.dao.TransferHistoryDao
import com.moin.project.domain.transfer.dto.TransferDto
import com.moin.project.domain.transfer.Quotation
import com.moin.project.domain.transfer.repository.QuotationRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional(readOnly = true)
class QuotationCRUD(
  private val quotationRepository: QuotationRepository,
) {

  /**
   * append
   */
  // 견적서 저장
  @Transactional
  fun appendQuotation(dto: TransferDto): Quotation {
    return quotationRepository.save(
      Quotation(
      user = dto.user,
      sourceAmount = dto.sourceAmount,
      targetCurrency = dto.targetCurrency,
      exchangeRate = dto.exchangeRate,
      fee = dto.fee,
      targetAmount = dto.targetAmount,
      expireTime = dto.expireTime,
      usdExchangeRate = dto.usdExchangeRate,
      usdAmount = dto.usdAmount
    )
    )
  }


  /**
   * find
   */
  // 오늘 요청한 송금 관련 정보 조회 - (유저 정보, 오늘 송금 횟수, 오늘 송금 USD)
  fun findTodayTransferQuotation(userSn: Long): TodayTransferDao {
    return quotationRepository.searchUserIdTypeAndTodayUsdAmount(userSn)
  }

  // 송금 요청한 이력(거래이력) 조회 - (송금 관련 정보)
  fun findAllTransferHistory(userSn: Long): List<TransferHistoryDao>? {
    return quotationRepository.searchTransferHistory(userSn)
  }

  // 견적서 조회 - (견적서 대한 정보)
  fun findQuotation(quotationId: Long, userSn: Long): Quotation {
    return quotationRepository.findByIdAndUserSnAndTransferRequest(quotationId, userSn, false)
      ?: throw CommonException(CommonExceptionCode.NOT_EXIST_QUOTE)
  }


  /**
   * update
   */
  // 견적서를 통한 송금 요청
  @Transactional
  fun requestQuotation(quotation: Quotation) {
    quotationRepository.save(quotation.updateRequest())
  }
}
