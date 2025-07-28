package com.example.shop.security.providers

import com.example.shop.security.models.ThirdPartyOauthAuthenticationToken
import com.example.shop.security.third_party_auth.user_services.ThirdPartyUserServiceManager
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.AuthenticationServiceException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.oauth2.jwt.JwtException

class ThirdPartyOauthAuthenticationProvider(
    private val thirdPartyAuthUserServiceManager: ThirdPartyUserServiceManager,
) : AuthenticationProvider {
    override fun authenticate(authentication: Authentication): Authentication {

        val authenticationToken = authentication as? ThirdPartyOauthAuthenticationToken
            ?: return authentication

        try {
            val user = thirdPartyAuthUserServiceManager.loadUser(authenticationToken.getTokenValue(), authenticationToken.getVendor())
            return ThirdPartyOauthAuthenticationToken(
                token = authenticationToken.getTokenValue(),
                vendor = authenticationToken.getVendor(),
                authorities = user.authorities,
            ).apply {
                isAuthenticated = true
                authenticatedUser = user
            }
        } catch (e: JwtException) {
            // JWT 검증 실패 (서명 오류, 만료, 유효하지 않은 클레임 등)
            throw BadCredentialsException("Invalid ID Token: ${e.message}", e)
        } catch (e: AuthenticationException) {
            // 다른 인증 관련 예외
            throw e
        } catch (e: Exception) {
            // 예상치 못한 예외
            throw AuthenticationServiceException("An error occurred during ID token authentication", e)
        }
    }

    override fun supports(authentication: Class<*>): Boolean {
        return ThirdPartyOauthAuthenticationToken::class.java.isAssignableFrom(authentication)
    }
}
