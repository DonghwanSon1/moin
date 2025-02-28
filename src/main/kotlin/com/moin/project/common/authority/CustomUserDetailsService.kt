package com.moin.project.common.authority

import com.moin.project.common.response.CustomUser
import com.moin.project.domain.user.User
import com.moin.project.domain.user.UserRepository
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(
    private val userRepository: UserRepository,
) : UserDetailsService {
    override fun loadUserByUsername(username: String): UserDetails =
        userRepository.findByUserId(username)
            ?.let { createUserDetails(it) } ?: throw UsernameNotFoundException("해당 유저는 없습니다.")

    private fun createUserDetails(user: User): UserDetails =
        CustomUser(
            user.sn!!,
            user.userId,
            user.password,
            listOf(SimpleGrantedAuthority(user.idType.toString()))
        )
}