package com.example.shop.security.third_party_auth.interfaces

import org.springframework.security.oauth2.core.oidc.user.OidcUser

interface ThirdPartyOidcUserService {
    fun loadUser(idToken: String): OidcUser
}
