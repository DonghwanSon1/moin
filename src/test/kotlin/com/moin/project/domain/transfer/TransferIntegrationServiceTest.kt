package com.moin.project.domain.transfer

import com.moin.project.common.exception.CommonException
import com.moin.project.common.exception.CommonExceptionCode
import com.moin.project.domain.transfer.enums.CurrencyInfo
import com.moin.project.domain.transfer.repository.QuotationRepository
import com.moin.project.domain.transfer.rqrs.QuoteRq
import com.moin.project.domain.transfer.rqrs.QuoteRs
import com.moin.project.domain.transfer.rqrs.TransferListRs
import com.moin.project.domain.transfer.rqrs.TransferRequestRq
import com.moin.project.domain.user.User
import com.moin.project.domain.user.UserRepository
import com.moin.project.domain.user.enums.Role
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

@SpringBootTest
class TransferIntegrationServiceTest @Autowired constructor(
  private val transferService: TransferService,
  private val quotationRepository: QuotationRepository,
  private val userRepository: UserRepository
) {

  @BeforeEach
  fun setup() {
    userRepository.saveAll(
      listOf(
        User(sn = 1L, userId = "testUser@naver.com", password = "암호화", name = "김개인", idType = Role.REG_NO, idValue = "암호화"),
        User(sn = 2L, userId = "corporation@naver.com", password = "암호화", name = "모인주식회사", idType = Role.BUSINESS_NO, idValue = "암호화")
      )
    )
  }

  @AfterEach
  fun clean() {
    quotationRepository.deleteAll()
  }

  /**
   * 견적서 저장 및 제공
   * - given / when / then
   *    1. 파라미터를 넣을 userSn, Rq를 생성한다.
   *    2. 견적서 제공 메서드(파라미터 포함)를 통해 견적서 요청한다.
   *    3. 견적서 제공 받은거와 견적서 테이블을 조회해 가져온 내용과 비교한다.
   *
   * - 테스트 확인
   *    1. 제공받은 견적서의 ID 와 가져온 견적서의 ID가 동일한지 확인.
   *    2. 제공받은 견적서의 환율과 가져온 견적서의 환율이 동일한지 확인.
   *    3. 제공받은 견적서의 유효시간과 가져온 견적서의 유효시간이 동일한지 확인.
   *    4. 제공받은 견적서의 받은금액과 가져온 견적서의 받을 금액이 동일한지 확인.
   */
  @Test
  fun `견적서 저장 및 제공`() {
    // given
    val userSn: Long = 1L
    val rq: QuoteRq = QuoteRq(amount = BigDecimal(10000), targetCurrency = CurrencyInfo.USD)

    // when
    val result: QuoteRs = transferService.transferQuote(userSn, rq)

    // then
    val quotation: Quotation = quotationRepository.findAll()[0]
    assertThat(result.quoteId).isEqualTo(quotation.id)
    assertThat(result.exchangeRate).isEqualTo(quotation.exchangeRate)
    assertThat(result.expireTime).isEqualTo(quotation.expireTime)
    assertThat(result.targetAmount).isEqualTo(quotation.targetAmount)
  }

  /**
   * 견적서 저장 및 제공 - 받을 금액 음수 (실패)
   * - given / when / then
   *    1. 파라미터를 넣을 userSn, Rq를 생성한다.
   *    2. 견적서 제공 메서드(파라미터 포함)를 통해 견적서 요청한다.
   *    3. 받을 금액이 음수가 나오게 유도한다.
   *
   * - 테스트 확인
   *    1. Exception Message 가 원하는 Message 인지 확인한다.
   */
  @Test
  fun `견적서 저장 및 제공 - 받을 금액 음수 (실패)`() {
    // given
    val userSn: Long = 1L
    val rq: QuoteRq = QuoteRq(amount = BigDecimal(1000), targetCurrency = CurrencyInfo.JPY)

    // when & then
    val message = assertThrows<CommonException> {
      transferService.transferQuote(userSn, rq)
    }.message

    assertThat(message).isEqualTo(CommonExceptionCode.NEGATIVE_NUMBER.message)
  }

  /**
   * 견적서를 통한 송금 요청
   * - given / when / then
   *    1. 견적서를 먼저 하나 저장(제공) 한다.
   *    2. 파라미터를 넣을 userSn, Rq를 생성한다.
   *    3. 송금 요청 메서드(파라미터 포함)를 통해 송금 요청한다.
   *
   * - 테스트 확인
   *    1. 송금 요청 후 받은 결과값 Message 가 동일한지 확인.
   *    2. 견적서를 조회 하여 가져와 송금 요청 시 '송금 요청 여부'가 True 로 변경되었는지 확인.
   *    3. 견적서를 조회 하여 가져와 '송금 요청 시간'이 null 이 아닌 값이 변경되었는지 확인.
   */
  @Test
  fun `견적서를 통한 송금 요청`() {
    // given
    val quotation: Quotation = quotationRepository.save(
      Quotation(
        user = User(sn = 1L, userId = "testUser@naver.com", password = "암호화", name = "김개인", idType = Role.REG_NO, idValue = "암호화"),
        sourceAmount = BigDecimal(10000),
        targetCurrency = CurrencyInfo.USD,
        exchangeRate = 1434.0,
        fee = BigDecimal(1020),
        targetAmount = BigDecimal(6.26),
        expireTime = LocalDateTime.now(ZoneId.of("Asia/Seoul")).plusMinutes(10),
        usdExchangeRate = 1434.0,
        usdAmount = BigDecimal(6.26)
      )
    )
    val userSn: Long = 1L
    val rq: TransferRequestRq = TransferRequestRq(quoteId = quotation.id!!)

    // when
    val result: String = transferService.transferRequest(userSn, rq)

    // then
    assertThat(result).isEqualTo("송금 접수 요청이 완료되었습니다.")
    val resultQuotation: Quotation = quotationRepository.findAll()[0]
    assertThat(resultQuotation.transferRequest).isTrue()
    assertThat(resultQuotation.requestedDate).isNotNull()
  }

  /**
   * 견적서를 통한 송금 요청 - 견적서의 유효시간이 지날 시 (실패)
   * - given / when / then
   *    1. 견적서를 먼저 하나 저장(제공) 한다. - 저장 시 유효시간을 지나도록 설정한다.
   *    2. 파라미터를 넣을 userSn, Rq를 생성한다.
   *    3. 송금 요청 메서드(파라미터 포함)를 통해 송금 요청한다.
   *
   * - 테스트 확인
   *    1. Exception Message 가 원하는 Message 인지 확인한다.
   */
  @Test
  fun `견적서를 통한 송금 요청 - 견적서의 유효시간이 지날 시 (실패)`() {
    // given
    val quotation: Quotation = quotationRepository.save(
      Quotation(
        user = User(sn = 1L, userId = "testUser@naver.com", password = "암호화", name = "김개인", idType = Role.REG_NO, idValue = "암호화"),
        sourceAmount = BigDecimal(10000),
        targetCurrency = CurrencyInfo.USD,
        exchangeRate = 1434.0,
        fee = BigDecimal(1020),
        targetAmount = BigDecimal(6.26),
        expireTime = LocalDateTime.now(ZoneId.of("Asia/Seoul")).minusMinutes(10),
        usdExchangeRate = 1434.0,
        usdAmount = BigDecimal(6.26)
      )
    )
    val userSn: Long = 1L
    val rq: TransferRequestRq = TransferRequestRq(quoteId = quotation.id!!)

    // when & then
    val message = assertThrows<CommonException> {
      transferService.transferRequest(userSn, rq)
    }.message

    assertThat(message).isEqualTo(CommonExceptionCode.QUOTE_EXPIRED.message)
  }

  /**
   * 견적서를 통한 송금 요청 - 개인 회원 1000불 이상 송금 요청 시 (실패)
   * - given / when / then
   *    1. 견적서를 두개 저장(제공) 한다.
   *    1.1 하나는 송금 요청이 완료된 견적서를 저장 한다. (691불)
   *    1.2 나머지 하나는 오늘 하루 총 송금 요청액이 1000불이 넘도록 400불 이상의 금액으로 견적서를 저장한다. (총 1000불 넘도록)
   *    2. 파라미터를 넣을 userSn, Rq를 생성한다.
   *    3. 송금 요청 메서드(파라미터 포함)를 통해 송금 요청한다. - 두번째 견적서를 송금 요청한다.
   *
   * - 테스트 확인
   *    1. Exception Message 가 원하는 Message 인지 확인한다.
   */
  @Test
  fun `견적서를 통한 송금 요청 - 개인 회원 1000불 이상 송금 요청 시 (실패)`() {
    // given
    val quotation: List<Quotation> = quotationRepository.saveAll(
      listOf(
        Quotation(
          user = User(sn = 1L, userId = "testUser@naver.com", password = "암호화", name = "김개인", idType = Role.REG_NO, idValue = "암호화"),
          sourceAmount = BigDecimal(1000000),
          targetCurrency = CurrencyInfo.JPY,
          exchangeRate = 	9.1716,
          fee = BigDecimal(8000),
          targetAmount = BigDecimal(108159),
          expireTime = LocalDateTime.now(ZoneId.of("Asia/Seoul")),
          usdExchangeRate = 1434.0,
          usdAmount = BigDecimal(691.77),
          requestedDate = LocalDateTime.now(ZoneId.of("Asia/Seoul")),
          transferRequest = true
        ),
        Quotation(
          user = User(sn = 1L, userId = "testUser@naver.com", password = "암호화", name = "김개인", idType = Role.REG_NO, idValue = "암호화"),
          sourceAmount = BigDecimal(700000),
          targetCurrency = CurrencyInfo.USD,
          exchangeRate = 1434.2,
          fee = BigDecimal(2400),
          targetAmount = BigDecimal(486.40),
          expireTime = LocalDateTime.now(ZoneId.of("Asia/Seoul")).plusMinutes(10),
          usdExchangeRate = 1434.2,
          usdAmount = BigDecimal(486.40),
        )
      )
    )
    val userSn: Long = 1L
    val rq: TransferRequestRq = TransferRequestRq(quoteId = quotation[1].id!!)

    // when & then
    val message = assertThrows<CommonException> {
      transferService.transferRequest(userSn, rq)
    }.message

    assertThat(message).isEqualTo(CommonExceptionCode.LIMIT_EXCESS.message)
  }

  /**
   * 견적서를 통한 송금 요청 - 법인 회원 5000불 이상 송금 요청 시 (실패)
   * - given / when / then
   *    1. 견적서를 두개 저장(제공) 한다.
   *    1.1 하나는 송금 요청이 완료된 견적서를 저장 한다. (4868불)
   *    1.2 나머지 하나는 오늘 하루 총 송금 요청액이 5000불이 넘도록 200불 이상의 금액으로 견적서를 저장한다. (총 5000불 넘도록)
   *    2. 파라미터를 넣을 userSn, Rq를 생성한다.
   *    3. 송금 요청 메서드(파라미터 포함)를 통해 송금 요청한다. - 두번째 견적서를 송금 요청한다.
   *
   * - 테스트 확인
   *    1. Exception Message 가 원하는 Message 인지 확인한다.
   */
  @Test
  fun `견적서를 통한 송금 요청 - 법인 회원 5000불 이상 송금 요청 시 (실패)`() {
    // given
    val quotation: List<Quotation> = quotationRepository.saveAll(
      listOf(
        Quotation(
          user = User(sn = 2L, userId = "corporation@naver.com", password = "암호화", name = "모인주식회사", idType = Role.BUSINESS_NO, idValue = "암호화"),
          sourceAmount = BigDecimal(7000000),
          targetCurrency = CurrencyInfo.JPY,
          exchangeRate = 	9.184,
          fee = BigDecimal(38000),
          targetAmount = BigDecimal(758057),
          expireTime = LocalDateTime.now(ZoneId.of("Asia/Seoul")),
          usdExchangeRate = 1430.0,
          usdAmount = BigDecimal(4868.53),
          requestedDate = LocalDateTime.now(ZoneId.of("Asia/Seoul")),
          transferRequest = true
        ),
        Quotation(
          user = User(sn = 2L, userId = "corporation@naver.com", password = "암호화", name = "모인주식회사", idType = Role.BUSINESS_NO, idValue = "암호화"),
          sourceAmount = BigDecimal(700000),
          targetCurrency = CurrencyInfo.USD,
          exchangeRate = 1434.2,
          fee = BigDecimal(2400),
          targetAmount = BigDecimal(486.40),
          expireTime = LocalDateTime.now(ZoneId.of("Asia/Seoul")).plusMinutes(10),
          usdExchangeRate = 1434.2,
          usdAmount = BigDecimal(486.40),
        )
      )
    )
    val userSn: Long = 2L
    val rq: TransferRequestRq = TransferRequestRq(quoteId = quotation[1].id!!)

    // when & then
    val message = assertThrows<CommonException> {
      transferService.transferRequest(userSn, rq)
    }.message

    assertThat(message).isEqualTo(CommonExceptionCode.LIMIT_EXCESS.message)
  }

  /**
   * 회원의 송금한 이력 조회
   * - given / when / then
   *    1. 송금 요청 완료된 견적서를 두개 저장 한다.
   *    2. 파라미터를 넣을 userSn 를 생성한다.
   *    3. 송금 이력 조회 메서드(파라미터 포함)를 통해 송금 이력 조회한다.
   *
   * - 테스트 확인
   *    1. 조회된 결과값의 유저 ID 가 저장된 유저 ID 가 동일한지 확인.
   *    2. 조회된 결과값의 유저 이름이 저장된 유저 이름과 동일한지 확인.
   *    3. 조회된 결과값의 오늘 송금 횟수가 2개가 맞는지 확인. (먼저 저장한 송금 요청 완료된 견적서 두개 - 둘다 오늘날짜로 저장)
   *    4. 조회된 결과값의 오늘 총 송금액이 4874.81 맞는지 확인. (먼저 저장한 송금 요청 완료된 견적서 두개에서 4868.53 + 6.28 = 4874.81)
   *    5. 조회된 결과값의 History 가 2개 인지 확인. (먼저 저장한 송금 요청 완료된 견적서 두개)
   */
  @Test
  fun `회원의 송금한 이력 조회`() {
    // given
    quotationRepository.saveAll(
      listOf(
        Quotation(
          user = User(sn = 2L, userId = "corporation@naver.com", password = "암호화", name = "모인주식회사", idType = Role.BUSINESS_NO, idValue = "암호화"),
          sourceAmount = BigDecimal(7000000),
          targetCurrency = CurrencyInfo.JPY,
          exchangeRate = 	9.184,
          fee = BigDecimal(38000),
          targetAmount = BigDecimal(758057),
          expireTime = LocalDateTime.now(ZoneId.of("Asia/Seoul")),
          usdExchangeRate = 1430.0,
          usdAmount = BigDecimal(4868.53),
          requestedDate = LocalDateTime.now(ZoneId.of("Asia/Seoul")),
          transferRequest = true
        ),
        Quotation(
          user = User(sn = 2L, userId = "corporation@naver.com", password = "암호화", name = "모인주식회사", idType = Role.BUSINESS_NO, idValue = "암호화"),
          sourceAmount = BigDecimal(10000),
          targetCurrency = CurrencyInfo.USD,
          exchangeRate = 1430.0,
          fee = BigDecimal(1020),
          targetAmount = BigDecimal(6.28),
          expireTime = LocalDateTime.now(ZoneId.of("Asia/Seoul")),
          usdExchangeRate = 1430.0,
          usdAmount = BigDecimal(6.28),
          requestedDate = LocalDateTime.now(ZoneId.of("Asia/Seoul")),
          transferRequest = true
        )
      )
    )
    val userSn: Long = 2L

    // when
    val result: TransferListRs = transferService.transferList(userSn)

    // then
    assertThat(result.userId).isEqualTo("corporation@naver.com")
    assertThat(result.name).isEqualTo("모인주식회사")
    assertThat(result.todayTransferCount).isEqualTo(2)
    assertThat(result.todayTransferUsdAmount).isEqualTo(BigDecimal(4874.81)
      .setScale(Currency.getInstance("USD").defaultFractionDigits, RoundingMode.HALF_UP))
    assertThat(result.history).size().isEqualTo(2)
  }
}