package com.example.shop.security.third_party_auth.interfaces

import org.springframework.security.oauth2.core.user.OAuth2User

interface ThirdPartyOauthUserService {
    fun loadUser(accessToken: String): OAuth2User
}
