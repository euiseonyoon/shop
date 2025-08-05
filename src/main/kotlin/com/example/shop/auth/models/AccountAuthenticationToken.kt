package com.example.shop.auth.models

import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority

class AccountAuthenticationToken(
    val accountId: Long,
    val authorities: List<GrantedAuthority>?,
    val email: String,
): AbstractAuthenticationToken(authorities) {
    override fun getCredentials(): Any? = null

    override fun getPrincipal(): Long = accountId
}

