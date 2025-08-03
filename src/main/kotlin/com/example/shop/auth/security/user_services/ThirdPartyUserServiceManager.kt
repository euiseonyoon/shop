package com.example.shop.auth.security.user_services

import com.example.shop.auth.models.CustomUserDetails
import com.example.shop.auth.security.third_party.enums.ThirdPartyAuthenticationVendor
import com.example.shop.auth.security.third_party.interfaces.ThirdPartyAuthenticationUserService
import org.springframework.security.authentication.AuthenticationServiceException
import org.springframework.security.core.userdetails.UserDetails

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
    ): UserDetails {
        val userService = findService(vendor)
        return userService.loadUser(token)
    }
}
