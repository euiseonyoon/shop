package com.example.shop.auth.security.user_services

import com.example.shop.auth.domain.AccountDomain
import com.example.shop.auth.domain.Email
import com.example.shop.auth.exceptions.LogInFailedException
import com.example.shop.auth.security.third_party.enums.ThirdPartyAuthenticationVendor
import com.example.shop.auth.security.third_party.interfaces.OidcDecodingAuthentication
import com.example.shop.auth.security.third_party.jwt_decoder.GoogleJwtDecoder

class GoogleOidcUserService(
    private val oauthAuthenticatedUserAutoRegisterer: OauthAuthenticatedUserAutoRegisterer,
) : OidcDecodingAuthentication {
    override val providerId: ThirdPartyAuthenticationVendor = ThirdPartyAuthenticationVendor.GOOGLE
    override val jwtDecoder = GoogleJwtDecoder()
    override val nameAttributeKey = "email"

    override fun getEmailAddressFromToken(token: String): String {
        try {
            val jwt = jwtDecoder.decode(token)
            return jwt.claims[nameAttributeKey] as String
        } catch (e: Exception) {
            // JWT 검증 실패 (서명 오류, 만료, 유효하지 않은 클레임 등)
            throw LogInFailedException("Invalid ID Token: ${e.message}")
        }
    }

    override fun findOrCreateUser(email: Email): AccountDomain {
        val result = oauthAuthenticatedUserAutoRegisterer.findOrCreateUser(
            email = email,
            providerId = this.providerId
        )
        return result.accountDomain
    }
}
