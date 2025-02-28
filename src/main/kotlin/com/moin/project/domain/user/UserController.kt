package com.moin.project.domain.user

import com.moin.project.common.authority.TokenInfo
import com.moin.project.common.response.BaseResponse
import com.moin.project.domain.user.rqrs.LoginRq
import com.moin.project.domain.user.rqrs.UserRq
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/user")
@Tag(name = "User", description = "유저 관련 API")
class UserController(
  private val userService: UserService,
) {

  /**
   * 회원가입 API
   */
  @PostMapping("/signup")
  @Operation(summary = "회원 가입", description = "회원 가입")
  fun signUp(@RequestBody @Valid userRq: UserRq): BaseResponse<Unit> {
    val resultMsg: String = userService.signUp(userRq)
    return BaseResponse(message = resultMsg)
  }

  /**
   * 로그인 (토큰 발급) API
   */
  @PostMapping("/login")
  @Operation(summary = "로그인", description = "로그인")
  fun login(@RequestBody @Valid loginRq: LoginRq): BaseResponse<TokenInfo> {
    val tokenInfo = userService.login(loginRq)
    return BaseResponse(data = tokenInfo)
  }

}