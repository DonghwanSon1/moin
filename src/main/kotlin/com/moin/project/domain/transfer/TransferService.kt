package com.moin.project.domain.transfer


import com.moin.project.common.exception.CommonException
import com.moin.project.common.exception.CommonExceptionCode
import com.moin.project.domain.transfer.component.QuotationCRUD
import com.moin.project.domain.transfer.component.TransferCalculate
import com.moin.project.domain.transfer.dao.TodayTransferDao
import com.moin.project.domain.transfer.dao.TransferHistoryDao
import com.moin.project.domain.transfer.dto.TransferCalculateDto
import com.moin.project.domain.transfer.dto.TransferDto
import com.moin.project.domain.transfer.dto.ExchangeRateDto
import com.moin.project.domain.transfer.enums.CurrencyInfo
import com.moin.project.domain.transfer.rqrs.*
import com.moin.project.domain.user.User
import com.moin.project.domain.user.UserCRUD
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.ZoneId

@Service
class TransferService(
  private val quotationCRUD: QuotationCRUD,
  private val userCRUD: UserCRUD,
  private val transferCalculate: TransferCalculate

) {

  /**
   * 견적서 저장 및 제공
   *
   * - 설명
   *  1. 유저 정보 가져온다.
   *  2. 송금에 필요한 계산된 값들을 가져온다. - (받는 금액이 음수면 Exception 발생시킨다.)
   *  3. 저장을 위해 송금 DTO 를 생성한다.
   *  4. 해당 DTO 를 통해 견적서를 생성/저장 한다.
   *  5. 저장한 견적서를 토대로 Rs 를 생성 하여 return 한다.
   */
  fun transferQuote(userSn: Long, rq: QuoteRq): QuoteRs {
    // 유저
    val user: User = userCRUD.findUser(userSn)

    // 계산된 값 DTO
    val transferCalculateDto: TransferCalculateDto = this.transferCalculate(rq)

    // 저장을 위한 DTO
    val transferDto: TransferDto = TransferDto.createTransferDto(rq, user, transferCalculateDto)

    // 저장한 견적서
    val quotation: Quotation = quotationCRUD.appendQuotation(transferDto)

    return QuoteRs.createQuoteRs(quotation)
  }

  /**
   * 견적서를 통한 송금 요청
   *
   * - 설명
   *  1. 견적서의 정보를 가져온다. (없을 시 Exception 발생)
   *  2. 견적서의 유효시간이 송금 요청한 시간보다 이전 시간이라면 Exception 발생시킨다.
   *  3. 오늘 하루동안의 송금한 금액의 합산과 유저 Type 을 가져온다. - (없으면 0으로 가져오기 때문에 !!이다.)
   *  4. 가져온 하루동안 송금한 금액의 합산과 견적서의 요청한 금액을 합산한 값을 가져온다.
   *  5. 합산된 금액이 유저의 Type 별로 지정된 값보다 크다면, Exception 발생 시킨다. - (개인 회원은 1000불 이상, 법인 회원은 5000불 이상)
   *  6. 송금 요청으로 변경하여 견적서를 저장 후 해당 String 을 Return 한다.
   */
  fun transferRequest(userSn: Long, rq: TransferRequestRq): String {
    // 견적서 정보
    val quotation: Quotation = quotationCRUD.findQuotation(rq.quoteId, userSn)

    // 견적서 유효시간 체크
    if (quotation.expireTime < LocalDateTime.now(ZoneId.of("Asia/Seoul")))
      throw CommonException(CommonExceptionCode.QUOTE_EXPIRED)

    // 오늘 하루동안의 송금한 금액의 합산 및 유저 Type 정보
    val todayTransferDao: TodayTransferDao = quotationCRUD.findTodayTransferQuotation(userSn)

    // 오늘 하루동안의 송금한 금액의 합산 + 송금할 금액
    val totalAmount: BigDecimal = transferCalculate.todayTotalAmountCalculate(quotation.usdAmount, todayTransferDao.todayTransferUsdAmount!!)

    // 회원 별 하루동안 송금 가능 액수 체크
    if (totalAmount >= todayTransferDao.idType!!.limitAmount) throw CommonException(CommonExceptionCode.LIMIT_EXCESS)

    quotationCRUD.requestQuotation(quotation)

    return "송금 접수 요청이 완료되었습니다."
  }

  /**
   * 회원의 송금한 이력 조회
   *
   * - 설명
   *  1. 오늘 하루동안의 송금한 금액의 합산 및 횟수와 유저 ID/이름 을 가져온다. - (없으면 0으로 가져오기 때문에 !!이다.)
   *  2. 회원의 지금까지 송금요청된 이력들을 가져온다. - (없으면 빈배열)
   *  3. Rs를 통해 Return 한다.
   */
  fun transferList(userSn: Long): TransferListRs {
    // 오늘 하루동안의 송금한 금액의 합산 및 횟수 와 유저 ID/이름 정보
    val todayTransferDao: TodayTransferDao = quotationCRUD.findTodayTransferQuotation(userSn)
    // 회원의 송금 이력 정보
    val transferHistoryDao: List<TransferHistoryDao>? = quotationCRUD.findAllTransferHistory(userSn)

    return TransferListRs(
      userId = todayTransferDao.userId,
      name = todayTransferDao.name,
      todayTransferCount = todayTransferDao.todayTransferCount,
      todayTransferUsdAmount = todayTransferDao.todayTransferUsdAmount,
      history = transferHistoryDao
    )
  }

  /**
   * 송금에 필요한 계산 로직 DTO
   *
   * - 설명 (계산 컴포넌트)
   *  1. 수수료를 계산하여 가져온다.
   *  2. 환율을 계산하여 가져온다.
   *  3. 송금 될 금액을 계산하여 가져온다. (송금될 금액이 음수면 Exception 발생시킨다.)
   *  4. Usd 금액을 계산하여 가져온다.
   *  5. 가져온 값들을 DTO 에 담아 return 한다.
   */
  private fun transferCalculate(rq: QuoteRq): TransferCalculateDto {
    // 수수료
    val fee: BigDecimal = transferCalculate.transferFeeCalculate(rq.amount, rq.targetCurrency)

    // 환율
    val exchangeRateDto: ExchangeRateDto = transferCalculate.transferExchangeRateCalculate(rq.targetCurrency)

    // 송금 될 금액 (송금 될 금액이 음수면 Exception)
    val targetAmount: BigDecimal = transferCalculate.transferTargetAmountCalculate(rq.amount, fee, rq.targetCurrency, exchangeRateDto.exchangeRate)
      .takeIf { it > BigDecimal.ZERO } ?: throw CommonException(CommonExceptionCode.NEGATIVE_NUMBER)

    // USD 금액
    val usdAmount: BigDecimal = transferCalculate.transferTargetAmountCalculate(rq.amount, fee, CurrencyInfo.USD, exchangeRateDto.usdExchangeRate)

    return TransferCalculateDto(
      exchangeRate = exchangeRateDto.exchangeRate.toDouble(),
      fee = fee,
      targetAmount = targetAmount,
      usdExchangeRate = exchangeRateDto.usdExchangeRate.toDouble(),
      usdAmount = usdAmount
    )
  }

}
