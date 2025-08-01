package com.example.shop.auth.models

import com.example.shop.auth.domain.Account
import org.springframework.security.authentication.AuthenticationServiceException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority

class UserIdEmailAuthenticationToken(
    private val account: Account,
    val authorities: List<GrantedAuthority>?
): UsernamePasswordAuthenticationToken(account, null, authorities) {
    init {
        if (account.username == null || account.id == null) {
            throw AuthenticationServiceException("account username and id should not be null.")
        }
    }

    fun getEmail(): String = account.username!!

    fun getAccountId(): Long = account.id!!
}
