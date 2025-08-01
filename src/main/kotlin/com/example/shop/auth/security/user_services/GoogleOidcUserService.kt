package com.example.shop.auth.security.user_services

import com.example.shop.auth.security.third_party.enums.ThirdPartyAuthenticationVendor
import com.example.shop.auth.security.third_party.interfaces.OidcDecodingAuthentication
import com.example.shop.auth.security.third_party.jwt_decoder.GoogleJwtDecoder
import com.example.shop.common.utils.CustomAuthorityUtils
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.oauth2.core.oidc.OidcIdToken
import org.springframework.security.oauth2.core.oidc.OidcUserInfo
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.security.oauth2.jwt.JwtException
import java.time.Instant

class GoogleOidcUserService(
    private val oauthAuthenticatedUserAutoRegisterer: OauthAuthenticatedUserAutoRegisterer,
    private val customAuthorityUtils: CustomAuthorityUtils,
) : OidcDecodingAuthentication {
    override val providerId: ThirdPartyAuthenticationVendor = ThirdPartyAuthenticationVendor.GOOGLE
    override val jwtDecoder = GoogleJwtDecoder()
    override val nameAttributeKey = "email"

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

            // Oauth2로 인증 된 유저를 회원 가입 시키거나, 만약 이미 있다면 유저 정보를 DB로 부터 가져온다 (authorities들을 사용하기 위해서)
            val accountResult = oauthAuthenticatedUserAutoRegisterer.findOrCreateUser(
                email = jwt.claims[nameAttributeKey] as String,
                providerId = this.providerId
            )
            val accountAuthorities = customAuthorityUtils.createSimpleGrantedAuthorities(accountResult.account)

            // 아래 accountAuthorities 는 access token/refresh token 만들때, claims에 넣도록 하자.
            return DefaultOidcUser(accountAuthorities, oidcIdToken, OidcUserInfo(jwt.claims), nameAttributeKey)
        } catch (e: JwtException) {
            // JWT 검증 실패 (서명 오류, 만료, 유효하지 않은 클레임 등)
            throw BadCredentialsException("Invalid ID Token: ${e.message}", e)
        }
    }
}
