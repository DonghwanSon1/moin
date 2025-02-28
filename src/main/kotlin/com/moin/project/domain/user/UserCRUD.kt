package com.moin.project.domain.user


import com.moin.project.common.exception.CommonException
import com.moin.project.common.exception.CommonExceptionCode
import org.springframework.transaction.annotation.Transactional
import org.springframework.stereotype.Component

@Component
@Transactional(readOnly = true)
class UserCRUD(
  private val userRepository: UserRepository,
) {

  /**
   * append
   */
  // 회원 저장
  @Transactional
  fun appendUser(user: User): User {
    return userRepository.save(user)
  }


  /**
   * find
   */
  // 회원 ID 중복 조회
  fun findDuplicateId(userId: String): User? {
    return userRepository.findByUserId(userId)
  }

  // 회원 Sn 정보 조회 - (없을 시 Exception 발생)
  fun findUser(userSn: Long): User {
    return userRepository.findById(userSn).orElseThrow { CommonException(CommonExceptionCode.NOT_EXIST_USER_ID) }
  }

}