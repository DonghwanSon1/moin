package com.moin.project.domain.transfer

import com.moin.project.common.exception.CommonException
import com.moin.project.common.exception.CommonExceptionCode
import com.moin.project.common.response.BaseResponse
import com.moin.project.common.response.CustomUser
import com.moin.project.domain.transfer.rqrs.QuoteRq
import com.moin.project.domain.transfer.rqrs.QuoteRs
import com.moin.project.domain.transfer.rqrs.TransferListRs
import com.moin.project.domain.transfer.rqrs.TransferRequestRq
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal


@RestController
@RequestMapping("/transfer")
@Tag(name = "Transfer", description = "송금 관련 API")
class TransferController(
  private val transferService: TransferService,
) {

  /**
   * 견적서 API
   */
  @PostMapping("/quote")
  @Operation(summary = "송금 견적서", description = "송금 견적서를 생성한다.")
  fun transferQuote(@AuthenticationPrincipal user: CustomUser?,
                    @RequestBody rq: QuoteRq): BaseResponse<QuoteRs> {
    if (rq.amount < BigDecimal(1)) throw CommonException(CommonExceptionCode.NEGATIVE_NUMBER)
    val userSn: Long = user?.sn ?: throw CommonException(CommonExceptionCode.INVALID_TOKEN)
    return BaseResponse(data = transferService.transferQuote(userSn, rq))
  }

  /**
   * 송금 접수 API
   */
  @PostMapping("/request")
  @Operation(summary = "송금 접수", description = "송금 견적서를 통해 송금 접수한다.")
  fun transferRequest(@AuthenticationPrincipal user: CustomUser?,
                      @RequestBody rq: TransferRequestRq): BaseResponse<Unit> {
    val userSn: Long = user?.sn ?: throw CommonException(CommonExceptionCode.INVALID_TOKEN)
    val resultMsg: String = transferService.transferRequest(userSn, rq)
    return BaseResponse(message = resultMsg)
  }

  /**
  * 송금 접수 API
  */
  @GetMapping("/list")
  @Operation(summary = "거래 이력", description = "회원의 모든 거래 이력을 조회한다.")
  fun transferList(@AuthenticationPrincipal user: CustomUser?): BaseResponse<TransferListRs> {
    val userSn: Long = user?.sn ?: throw CommonException(CommonExceptionCode.INVALID_TOKEN)
    return BaseResponse(data = transferService.transferList(userSn))
  }

}