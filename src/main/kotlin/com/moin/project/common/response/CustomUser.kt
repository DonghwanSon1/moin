package com.moin.project.common.response

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.User

class CustomUser(
    val sn: Long,
    userId: String,
    password: String,
    authorities: Collection<GrantedAuthority>
) : User(userId, password, authorities)