package com.example.shop.security.third_party_auth.user_services

import com.example.shop.security.third_party_auth.enums.ThirdPartyAuthenticationVendor
import com.example.shop.security.third_party_auth.interfaces.OidcNameAttributeKeyInterface
import com.example.shop.security.third_party_auth.interfaces.ThirdPartyAuthenticationUserService
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.oauth2.core.oidc.OidcIdToken
import org.springframework.security.oauth2.core.oidc.OidcUserInfo
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtException
import java.time.Instant

class GoogleOidcUserService(
    private val jwtDecoder: JwtDecoder
) : ThirdPartyAuthenticationUserService, OidcNameAttributeKeyInterface {
    override val nameAttributeKey = "email"

    override val providerId = ThirdPartyAuthenticationVendor.GOOGLE

    override fun loadUser(token: String): OidcUser {
        try {
            val jwt = jwtDecoder.decode(token)

            val oidcIdToken = OidcIdToken(
                token,
                jwt.issuedAt ?: Instant.now(),
                jwt.expiresAt ?: Instant.MAX,
                jwt.claims
            )
            // jwt.claims.iss = "https://accounts.google.com
            // jwt.claims.email_verified = true
            return DefaultOidcUser(emptyList(), oidcIdToken, OidcUserInfo(jwt.claims), nameAttributeKey)
        } catch (e: JwtException) {
            // JWT 검증 실패 (서명 오류, 만료, 유효하지 않은 클레임 등)
            throw BadCredentialsException("Invalid ID Token: ${e.message}", e)
        }
    }


}
