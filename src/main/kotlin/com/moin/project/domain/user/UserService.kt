package com.moin.project.domain.user


import com.moin.project.common.authority.EncryptionUtil
import com.moin.project.common.authority.JwtTokenProvider
import com.moin.project.common.authority.TokenInfo
import com.moin.project.common.exception.CommonException
import com.moin.project.common.exception.CommonExceptionCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import com.moin.project.domain.user.rqrs.LoginRq
import com.moin.project.domain.user.rqrs.UserRq
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.core.AuthenticationException
import org.springframework.security.crypto.password.PasswordEncoder

@Service
class UserService(
  private val userCRUD: UserCRUD,
  private val authenticationManagerBuilder: AuthenticationManagerBuilder,
  private val jwtTokenProvider: JwtTokenProvider,
  private val passwordEncoder: PasswordEncoder,
  private val encryptionUtil: EncryptionUtil,
) {

  /**
   * 회원가입
   *
   * - 설명
   *  1. userId 에 대해 중복된 ID 가 있는지 체크한다. - (있을 시 Exception 발생)
   *  2. User 를 생성 한다. (비밀번호는 BCrypt 암호화, 주민/사업자 번호는 AES-256 암호화)
   *  3. 해당 User 를 저장 후 해당 결과값 String 을 Return 한다.
   */
  fun signUp(userRq: UserRq): String {
    var user: User? = userCRUD.findDuplicateId(userRq.userId)
    if (user != null) throw CommonException(CommonExceptionCode.DUPLICATE_ID)

    user = User.createUser(userRq, passwordEncoder.encode(userRq.password), encryptionUtil.encrypt(userRq.idValue))
    userCRUD.appendUser(user)

    return "회원가입이 완료되었습니다."
  }

  /**
   * 로그인 (토큰 발행)
   *
   * - 설명
   *  1. 로그인 할 ID/PW 를 UsernamePasswordAuthenticationToken 통해 인증 준비한다.
   *  2. AuthenticationManager 를 통해 준비된 값을 인증한다. - (실패 시 발생하는 Exception 을 try Catch 로 잡아 CustomException 으로 발생시킨다.)
   *  3. 인증된 값을 JWT 를 통해 토큰 발급 하며 토큰값을 Return 한다.
   */
  fun login(loginRq: LoginRq): TokenInfo {
    try {
      val authenticationToken = UsernamePasswordAuthenticationToken(loginRq.userId, loginRq.password)
      val authentication = authenticationManagerBuilder.`object`.authenticate(authenticationToken)

      return jwtTokenProvider.createToken(authentication)
    } catch (e: AuthenticationException) {
      throw CommonException(CommonExceptionCode.LOGIN_FAIL)
    }
  }
}