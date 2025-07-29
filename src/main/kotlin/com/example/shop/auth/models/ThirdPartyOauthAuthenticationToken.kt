package com.example.shop.auth.models

import com.example.shop.auth.security.third_party.enums.ThirdPartyAuthenticationVendor
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.core.user.OAuth2User

class ThirdPartyOauthAuthenticationToken(
    // OIDC인 경우, idToken, Oauth2.0 인경우, accessToken
    private val token: String,
    private val vendor: ThirdPartyAuthenticationVendor,
    authorities: Collection<GrantedAuthority>,
) : AbstractAuthenticationToken(authorities) {

    var authenticatedUser: OAuth2User? = null

    init {
        isAuthenticated = false
    }

    override fun getCredentials(): String = token

    override fun getPrincipal(): Any = authenticatedUser ?: token

    fun getTokenValue(): String = token

    fun getVendor(): ThirdPartyAuthenticationVendor = vendor
}
