package com.example.shop.auth.security.user_services

import com.example.shop.auth.domain.AccountDomain
import com.example.shop.auth.domain.Email
import com.example.shop.auth.security.third_party.enums.ThirdPartyAuthenticationVendor
import com.example.shop.auth.security.third_party.interfaces.ThirdPartyAuthenticationUserService
import org.springframework.security.authentication.AuthenticationServiceException

class ThirdPartyUserServiceManager(
    private val userServiceList: List<ThirdPartyAuthenticationUserService>
) {
    fun findService(vendor: ThirdPartyAuthenticationVendor): ThirdPartyAuthenticationUserService {
        return userServiceList.find { it.supports(vendor) }
            ?: throw AuthenticationServiceException("No matching user service found for provider '$vendor'")
    }

    fun findOrCreateAccount(
        email: Email,
        userService: ThirdPartyAuthenticationUserService
    ): AccountDomain {
        return userService.findOrCreateUser(email)
    }
}
