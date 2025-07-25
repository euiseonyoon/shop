package com.example.shop.security

import com.example.shop.security.models.ThirdPartyOauthAuthenticationToken
import com.example.shop.security.third_party_auth.interfaces.ThirdPartyOidcUserService
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.AuthenticationServiceException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.oauth2.jwt.JwtException

class ThirdPartyOauthAuthenticationProvider(
    private val googleOidcUserService: ThirdPartyOidcUserService,
) : AuthenticationProvider {
    override fun authenticate(authentication: Authentication): Authentication {

        val idTokenAuthToken = authentication as? ThirdPartyOauthAuthenticationToken
            ?: return authentication

        try {
            val user = googleOidcUserService.loadUser(idTokenAuthToken.getTokenValue())
            val idTokenString = user.idToken.tokenValue
            return ThirdPartyOauthAuthenticationToken(idTokenString, user.authorities).apply {
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
