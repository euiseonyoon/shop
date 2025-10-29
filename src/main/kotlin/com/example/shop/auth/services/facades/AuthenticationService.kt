package com.example.shop.auth.services.facades

import com.example.shop.auth.domain.AccountDomain
import com.example.shop.auth.domain.Email
import com.example.shop.auth.security.third_party.enums.ThirdPartyAuthenticationVendor
import com.example.shop.auth.security.third_party.interfaces.ThirdPartyAuthenticationUserService
import com.example.shop.auth.security.user_services.ThirdPartyUserServiceManager
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthenticationService(
    private val passwordEncoder: PasswordEncoder,
    private val thirdPartyUserServiceManager: ThirdPartyUserServiceManager,
) {
    fun validatePassword(rawPassword: String, passwordHash: String): Boolean {
        return passwordHash == passwordEncoder.encode(rawPassword)
    }

    fun getVendorService(vendor: ThirdPartyAuthenticationVendor): ThirdPartyAuthenticationUserService {
        return thirdPartyUserServiceManager.findService(vendor)
    }

    fun findOrCreateAccount(email: Email, service: ThirdPartyAuthenticationUserService): AccountDomain {
        return thirdPartyUserServiceManager.findOrCreateAccount(email, service)
    }
}

