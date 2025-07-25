package com.example.shop.security.third_party_auth.user_services

import com.example.shop.security.third_party_auth.enums.ThirdPartyAuthenticationVendor
import com.example.shop.security.third_party_auth.interfaces.ThirdPartyAuthenticationUserService
import org.springframework.security.authentication.AuthenticationServiceException
import org.springframework.security.oauth2.core.user.OAuth2User

class ThirdPartyUserServiceManager(
    private val userServiceList: List<ThirdPartyAuthenticationUserService>
) {
    fun findService(vendor: ThirdPartyAuthenticationVendor): ThirdPartyAuthenticationUserService {
        return userServiceList.find { it.supports(vendor) }
            ?: throw AuthenticationServiceException("No matching user service found for provider '$vendor'")
    }

    fun loadUser(
        token: String,
        vendor: ThirdPartyAuthenticationVendor,
    ): OAuth2User {
        val userService = findService(vendor)
        return userService.loadUser(token)
    }
}
