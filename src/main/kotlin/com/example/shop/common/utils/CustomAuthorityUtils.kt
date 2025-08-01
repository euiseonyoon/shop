package com.example.shop.common.utils

import com.example.shop.auth.domain.Account
import org.springframework.security.core.authority.SimpleGrantedAuthority

interface CustomAuthorityUtils {
    fun validateAuthorityPrefix(roleName: String)

    fun createSimpleGrantedAuthorities(account: Account): List<SimpleGrantedAuthority>
}
