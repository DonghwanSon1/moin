package com.moin.project.domain.user

import com.moin.project.common.authority.EncryptionUtil
import com.moin.project.common.authority.JwtTokenProvider
import com.moin.project.common.exception.CommonException
import com.moin.project.common.exception.CommonExceptionCode
import com.moin.project.common.response.CustomUser
import com.moin.project.domain.user.enums.Role
import com.moin.project.domain.user.rqrs.LoginRq
import com.moin.project.domain.user.rqrs.UserRq
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.crypto.password.PasswordEncoder

@SpringBootTest
class UserServiceIntegrationTest @Autowired constructor(
  private val userService: UserService,
  private val userRepository: UserRepository,
  private val jwtTokenProvider: JwtTokenProvider,
  private val passwordEncoder: PasswordEncoder,
  private val encryptionUtil: EncryptionUtil,
) {

  @AfterEach
  fun clean() {
    userRepository.deleteAll()
  }

  /**
   * 개인 회원 회원가입
   * - given / when / then
   *    1. 회원가입 Rq를 생성한다.
   *    2. 회원가입 메서드를 통해 회원가입을 요청한다.
   *    3. 유저 테이블을 조회한 후 Rq와 비교한다.
   *
   * - 테스트 확인
   *    1. 저장된게 하나인지 확인.
   *    2. Rq 에서의 userId와 저장된 값에서 userId 가 일치하는지 확인.
   *    3. 저장된 Password(암호화 된) 를 가져오고, Rq로 보낸 평문이 아닌지 확인
   *    4. 저장된 idValue(암호화 된) 를 가져오고, Rq로 보낸 평문이 아닌지 확인.
   */
  @Test
  fun `개인 회원 - 회원 가입`() {
    // given
    val userRq = UserRq(_userId = "testUser@naver.com", password = "admin", name = "김개인", idType = Role.REG_NO, idValue = "970514-1112311")

    // when
    userService.signUp(userRq)

    // then
    val user = userRepository.findAll()
    assertThat(user).hasSize(1)
    assertThat(user[0].userId).isEqualTo("testUser@naver.com")
    assertThat(user[0].password).isNotEqualTo(userRq.password)
    assertThat(user[0].idValue).isNotEqualTo(userRq.idValue)
  }

  /**
   * 법인 회원 회원가입
   * - given / when / then
   *    1. 회원가입 Rq를 생성한다.
   *    2. 회원가입 메서드를 통해 회원가입을 요청한다.
   *    3. 유저 테이블을 조회한 후 Rq와 비교한다.
   *
   * - 테스트 확인
   *    1. 저장된게 하나인지 확인.
   *    2. Rq 에서의 userId와 저장된 값에서 userId 가 일치하는지 확인.
   *    3. 저장된 Password(암호화 된) 를 가져오고, Rq로 보낸 평문이 아닌지 확인
   *    4. 저장된 idValue(암호화 된) 를 가져오고, Rq로 보낸 평문이 아닌지 확인.
   */
  @Test
  fun `법인 회원 - 회원 가입`() {
    // given
    val userRq = UserRq(_userId = "testBusiness@moin.com", password = "admin", name = "김법인", idType = Role.BUSINESS_NO, idValue = "123-45-67890")

    // when
    userService.signUp(userRq)

    // then
    val user = userRepository.findAll()
    assertThat(user).hasSize(1)
    assertThat(user[0].userId).isEqualTo("testBusiness@moin.com")
    assertThat(user[0].password).isNotEqualTo(userRq.password)
    assertThat(user[0].idValue).isNotEqualTo(userRq.idValue)
  }

  /**
   * 회원 가입 실패 - 중복 ID
   * - given / when / then
   *    1. 먼저 유저를 저장한다.
   *    2. 저장한 유저와 동일한 userId를 가진 Rq를 생성한다.
   *    3. 회원가입 메서드를 통해 회원가입을 요청한다.
   *
   * - 테스트 확인
   *    1. 중복 ID 회원가입 시 발생하는 Exception 의 Message 를 가져온 후 발생된 Message 가 일치 하는지 확인.
   */
  @Test
  fun `회원 가입 실패 - 중복 ID`() {
    // given
    userRepository.save(
      User(sn = 1, userId = "testUser@naver.com", password = passwordEncoder.encode("admin"),
        name = "김개인", idType = Role.REG_NO, idValue = encryptionUtil.encrypt("970514-1112311"))
    )
    val userRq = UserRq(_userId = "testUser@naver.com", password = "admin", name = "김개인", idType = Role.REG_NO, idValue = "970514-1112311")

    // when & then
    val message = assertThrows<CommonException> {
      userService.signUp(userRq)
    }.message

    assertThat(message).isEqualTo(CommonExceptionCode.DUPLICATE_ID.message)
  }

  /**
   * 로그인
   * - given / when / then
   *    1. 로그인 할 유저를 먼저 저장한다.
   *    2. loginRq 를 저장한 유저의 내용으로 Rq 를 생성한다.
   *    3. 로그인 메서드를 통해 로그인 요청한다.
   *
   * - 테스트 확인
   *    1. 결과값이 isNotNull 인지 확인.
   *    2. 결과값의 token 이 비어 있지 않은걸 확인.
   *
   */
  @Test
  fun `로그인`() {
    // given
    userRepository.save(
      User(sn = null, userId = "testUser@naver.com", password = passwordEncoder.encode("admin"),
        name = "김개인", idType = Role.REG_NO, idValue = encryptionUtil.encrypt("970514-1112311"))
    )
    val loginRq = LoginRq(_userId = "testUser@naver.com", password = "admin")

    // when
    val result= userService.login(loginRq)

    // then
    assertThat(result).isNotNull
    assertThat(result.token).isNotEmpty()
  }

  /**
   * 로그인 토큰 정보 검증
   * - given / when / then
   *    1. 로그인 할 유저를 먼저 저장한다.
   *    2. loginRq 를 저장한 유저의 내용으로 Rq 를 생성한다.
   *    3. 로그인 메서드를 통해 로그인 요청한다.
   *    4. 로그인 시 받은 토큰을 가지고 CustomUser 로 가져온다.
   *
   * - 테스트 확인
   *    1. Token 의 정보를 추출해서 ID 가 동일한지 확인.
   *    2. Token 의 정보를 추출해서 Role 이 동일한지 확인.
   *
   */
  @Test
  fun `로그인 토큰 정보 검증`() {
    // given
    val user: User = userRepository.save(
      User(sn = null, userId = "testUser@naver.com", password = passwordEncoder.encode("admin"),
        name = "김개인", idType = Role.REG_NO, idValue = encryptionUtil.encrypt("970514-1112311"))
    )
    val loginRq = LoginRq(_userId = "testUser@naver.com", password = "admin")

    // when
    val result= userService.login(loginRq)
    val authentication = jwtTokenProvider.getAuthentication(result.token)
    val customUser: CustomUser = authentication.principal as CustomUser

    // then
    assertThat(customUser.sn).isEqualTo(user.sn)
    assertThat(customUser.authorities.first().toString()).isEqualTo(Role.REG_NO.name)
  }

  /**
   * 로그인 실패 - 인증 실패
   * - given / when / then
   *    1. 로그인 할 유저를 먼저 저장한다.
   *    2. 로그인 실패 할 유저를 loginRq를 생성한다. - (비밀번호 틀림)
   *    3. 로그인 메서드를 통해 로그인을 요청한다.
   *
   * - 테스트 확인
   *    1. 로그인 실패 시 발생하는 Exception 의 Message 를 가져온 후 발생된 Message 가 일치 하는지 확인.
   */
  @Test
  fun `로그인 실패 - 인증 실패`() {
    // given
    userRepository.save(
      User(sn = 1, userId = "testUser@naver.com", password = passwordEncoder.encode("admin"),
        name = "김개인", idType = Role.REG_NO, idValue = encryptionUtil.encrypt("970514-1112311"))
    )
    val loginRq = LoginRq(_userId = "testUser@naver.com", password = "admin123123")

    // when & then
    val message = assertThrows<CommonException> {
      userService.login(loginRq)
    }.message

    assertThat(message).isEqualTo(CommonExceptionCode.LOGIN_FAIL.message)
  }


}