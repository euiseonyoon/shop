package com.example.shop.common.utils

import com.example.shop.auth.ROLE_PREFIX
import com.example.shop.auth.domain.Account
import com.example.shop.common.utils.exceptions.AuthorityPrefixException
import org.springframework.security.core.authority.SimpleGrantedAuthority

class AuthorityUtils {
    companion object {
        fun validateAuthorityPrefix(roleName: String) {
            if (!roleName.startsWith(ROLE_PREFIX)) {
                throw AuthorityPrefixException(roleName)
            }
        }

        fun createSimpleGrantedAuthorities(account: Account): List<SimpleGrantedAuthority> {
            val authority = account.authority!!.let { SimpleGrantedAuthority(it.roleName!!) }
            val groupAuthorities = account.getGroupAuthorities().mapNotNull {
                it.name?.let { SimpleGrantedAuthority(it) }
            }
            return groupAuthorities + authority
        }
    }
}
