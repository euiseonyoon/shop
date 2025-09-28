package com.example.shop.auth.security.user_services

import com.example.shop.auth.domain.Email
import com.example.shop.auth.models.CustomUserDetails
import com.example.shop.auth.security.third_party.enums.ThirdPartyAuthenticationVendor
import com.example.shop.auth.security.third_party.interfaces.OidcDecodingAuthentication
import com.example.shop.auth.security.third_party.jwt_decoder.GoogleJwtDecoder
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.oauth2.jwt.JwtException

class GoogleOidcUserService(
    private val oauthAuthenticatedUserAutoRegisterer: OauthAuthenticatedUserAutoRegisterer,
) : OidcDecodingAuthentication {
    override val providerId: ThirdPartyAuthenticationVendor = ThirdPartyAuthenticationVendor.GOOGLE
    override val jwtDecoder = GoogleJwtDecoder()
    override val nameAttributeKey = "email"

    override fun getEmailAddress(token: String): String {
        val jwt = jwtDecoder.decode(token)
        return jwt.claims[nameAttributeKey] as String
    }

    override fun loadUser(token: String): UserDetails {
        try {
            // Oauth2로 인증 된 유저를 회원 가입 시키거나, 만약 이미 있다면 유저 정보를 DB로 부터 가져온다 (authorities들을 사용하기 위해서)
            val result = oauthAuthenticatedUserAutoRegisterer.findOrCreateUser(
                email = Email(getEmailAddress(token)),
                providerId = this.providerId
            )

            return CustomUserDetails(
                account = result.accountDomain.account,
                allAuthorities = result.accountDomain.authorities,
            )
        } catch (e: JwtException) {
            // JWT 검증 실패 (서명 오류, 만료, 유효하지 않은 클레임 등)
            throw BadCredentialsException("Invalid ID Token: ${e.message}", e)
        }
    }
}
