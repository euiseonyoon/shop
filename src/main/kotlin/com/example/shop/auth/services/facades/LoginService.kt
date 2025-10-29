package com.example.shop.auth.services.facades

import com.example.shop.auth.domain.Email
import com.example.shop.auth.exceptions.LogInFailedException
import com.example.shop.auth.models.TokenResponse
import com.example.shop.auth.security.handlers.LogInSuccessHandler
import com.example.shop.auth.security.third_party.enums.ThirdPartyAuthenticationVendor
import com.example.shop.auth.services.AccountDomainService
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class LoginService(
    private val accountDomainService: AccountDomainService,
    private val authenticationService: AuthenticationService,
    private val logInSuccessHandler: LogInSuccessHandler,
) {
    @Transactional
    fun emailLogin(email: Email, rawPassword: String, response: HttpServletResponse): TokenResponse {
        val accountDomain = accountDomainService.findByEmail(email) ?: throw LogInFailedException("유저를 찾을 수 없음.")

        if (!authenticationService.validatePassword(rawPassword, accountDomain.account.passwordHash)) {
            throw LogInFailedException("잘못된 비밀번호")
        }

        return logInSuccessHandler.getTokenResponse(response, accountDomain)
    }

    @Transactional
    fun oauthLogin(
        vendor: ThirdPartyAuthenticationVendor,
        token: String,
        response: HttpServletResponse,
    ): TokenResponse {
        val vendorService = authenticationService.getVendorService(vendor)
        val email = Email(vendorService.getEmailAddressFromToken(token))

        val accountDomain = authenticationService.findOrCreateAccount(email, vendorService)

        return logInSuccessHandler.getTokenResponse(response, accountDomain)
    }
}
