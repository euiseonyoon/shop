package com.example.shop.common.utils

import com.example.shop.auth.ROLE_PREFIX
import com.example.shop.auth.domain.Account
import com.example.shop.common.utils.exceptions.AuthorityPrefixException
import org.springframework.security.authentication.AuthenticationServiceException
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component

@Component
class CustomAuthorityUtilsImpl : CustomAuthorityUtils {
    override fun validateAuthorityPrefix(roleName: String) {
        if (!roleName.startsWith(ROLE_PREFIX)) {
            throw AuthorityPrefixException(roleName)
        }
    }

    override fun createSimpleGrantedAuthorities(account: Account): List<SimpleGrantedAuthority> {
        val authorityString = account.authority?.roleName
            ?: throw AuthenticationServiceException("Account is missing a role. Account must have a single role.")

        val authority = SimpleGrantedAuthority(authorityString)
        val groupAuthorities = account.getGroupAuthorities().mapNotNull {
            it.name?.let { SimpleGrantedAuthority(it) }
        }
        return groupAuthorities + authority
    }
}
