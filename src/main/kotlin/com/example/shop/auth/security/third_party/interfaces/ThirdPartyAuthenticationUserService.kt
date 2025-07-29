package com.example.shop.auth.security.third_party.interfaces

import com.example.shop.auth.security.third_party.enums.ThirdPartyAuthenticationVendor
import org.springframework.security.oauth2.core.user.OAuth2User

interface ThirdPartyAuthenticationUserService {
    val providerId: ThirdPartyAuthenticationVendor

    fun supports(vendor: ThirdPartyAuthenticationVendor): Boolean = providerId == vendor

    fun loadUser(token: String): OAuth2User
}
