package com.example.shop.auth.models

import com.example.shop.auth.domain.Account
import org.springframework.security.authentication.AuthenticationServiceException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority

class AccountAuthenticationToken(
    private val account: Account,
    val authorities: List<GrantedAuthority>?
): UsernamePasswordAuthenticationToken(account, null, authorities) {
    init {
        if (account.email == null || account.id == null) {
            throw AuthenticationServiceException("account email and id should not be null.")
        }
    }

    fun getEmail(): String = account.email!!

    fun getAccountId(): Long = account.id!!
}
