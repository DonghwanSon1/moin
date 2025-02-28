package com.moin.project.domain.user

import com.moin.project.common.authority.EncryptionUtil
import com.moin.project.common.authority.JwtTokenProvider
import com.moin.project.common.authority.TokenInfo
import com.moin.project.common.exception.CommonException
import com.moin.project.common.exception.CommonExceptionCode
import com.moin.project.domain.user.enums.Role
import com.moin.project.domain.user.rqrs.LoginRq
import com.moin.project.domain.user.rqrs.UserRq
import io.mockk.*
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.password.PasswordEncoder

class UserServiceUnitTest {

  private val userCRUD: UserCRUD = mockk<UserCRUD>()
  private val authenticationManagerBuilder: AuthenticationManagerBuilder = mockk<AuthenticationManagerBuilder>()
  private val jwtTokenProvider: JwtTokenProvider = mockk<JwtTokenProvider>()
  private val passwordEncoder: PasswordEncoder = mockk<PasswordEncoder>()
  private val encryptionUtil: EncryptionUtil = mockk<EncryptionUtil>()
  private val userService: UserService = UserService(
    userCRUD,
    authenticationManagerBuilder,
    jwtTokenProvider,
    passwordEncoder,
    encryptionUtil
  )

  @Test
  fun `회원가입 성공`() {
    // given
    val userRq = UserRq(_userId = "testUser@naver.com", password = "admin", name = "테스트", idType = Role.REG_NO, idValue = "970514-1112311")
    every { userCRUD.findDuplicateId(userRq.userId) } returns null
    every { passwordEncoder.encode(userRq.password) } returns "{bcrypt}\$2a\$10\$0sLKphEOIJqo9WaIIegJvewcAE/L2BnJcLKdbI7poUIZzqr6BHfWm"
    every { encryptionUtil.encrypt(userRq.idValue) } returns "wN0u/5XMUmUKmuHcz104eQ=="
    every { userCRUD.appendUser(any()) } answers { firstArg() }

    // when
    val result = userService.signUp(userRq)

    // then
    assertThat(result).isEqualTo("회원가입이 완료되었습니다.")
  }

  @Test
  fun `회원가입 실패 - 중복된 ID`() {
    // Given
    val userRq = UserRq(_userId = "testUser@naver.com", password = "admin", name = "테스트", idType = Role.REG_NO, idValue = "970514-1112311")
    every { userCRUD.findDuplicateId(userRq.userId) } returns mockk()

    // When & Then
    val message = assertThrows<CommonException> {
      userService.signUp(userRq)
    }.message

    assertThat(message).isEqualTo(CommonExceptionCode.DUPLICATE_ID.message)
  }

  @Test
  fun `로그인 성공`() {
    // Given
    val loginRq = LoginRq(_userId = "testUser@naver.com", password = "admin")
    val authenticationToken = UsernamePasswordAuthenticationToken(loginRq.userId, loginRq.password)
    val authentication: Authentication = mockk()
    every { authenticationManagerBuilder.`object`.authenticate(authenticationToken) } returns authentication
    every { jwtTokenProvider.createToken(authentication) } returns TokenInfo("accessToken")

    // When
    val tokenInfo = userService.login(loginRq)

    // Then
    assertNotNull(tokenInfo)
    assertEquals("accessToken", tokenInfo.token)
  }

  @Test
  fun `로그인 실패 - 인증 실패`() {
    // Given
    val loginRq = LoginRq(_userId = "testUser@naver.com", password = "admin")
    val authenticationToken = UsernamePasswordAuthenticationToken(loginRq.userId, loginRq.password)
    every { authenticationManagerBuilder.`object`.authenticate(authenticationToken) } throws BadCredentialsException("유효하지 않은 Credential")

    // When & Then
    val exception = assertThrows<CommonException> {
      userService.login(loginRq)
    }
    assertEquals(CommonExceptionCode.LOGIN_FAIL, exception.exceptionCode)
  }

}