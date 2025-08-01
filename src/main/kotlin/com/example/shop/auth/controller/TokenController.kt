package com.example.shop.auth.controller

import com.example.shop.auth.TOKEN_REFRESH_URI
import com.example.shop.auth.exceptions.RefreshTokenMissingException
import com.example.shop.auth.jwt_helpers.MyJwtTokenHelper
import com.example.shop.auth.models.TokenResponse
import com.example.shop.auth.security.utils.MyJwtTokenExtractor
import com.example.shop.auth.services.AccountService
import com.example.shop.common.apis.GlobalResponse
import com.example.shop.common.utils.CustomAuthorityUtils
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.AuthenticationServiceException
import org.springframework.security.oauth2.jwt.JwtException
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class TokenController(
    private val myJwtTokenHelper: MyJwtTokenHelper,
    private val accountService: AccountService,
    private val myJwtTokenExtractor: MyJwtTokenExtractor,
    private val customAuthorityUtils: CustomAuthorityUtils
) {

    @PostMapping(TOKEN_REFRESH_URI)
    fun refreshTokens(
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): GlobalResponse<TokenResponse> {
        val refreshToken = myJwtTokenExtractor.extractRefreshTokenFromCookie(request)
            ?: throw RefreshTokenMissingException("Can't find refresh token from the cookie.")

        try {
            val claims = myJwtTokenHelper.parseRefreshToken(refreshToken)
            val email = myJwtTokenHelper.getAccountEmail(claims)
            val account = accountService.findWithAuthoritiesByEmail(email)
                ?: throw AuthenticationServiceException("Can't find the account from the database.")

            val newAccessToken = myJwtTokenHelper.createAccessToken(
                email,
                customAuthorityUtils.createSimpleGrantedAuthorities(account)
            )
            // TODO: 더 강력한 보안을 위해 Refresh 토큰도 다시 생성한다. 따라서 Refresh 토큰의 state를 관리해야한다.
            // Login 하면서 발행한 refresh 토큰의 상태도 관리 해야한다.. redis 사용?
            val newRefreshToken = myJwtTokenHelper.createRefreshToken(email)

            myJwtTokenHelper.setRefreshTokenOnCookie(response, newRefreshToken)
            return GlobalResponse.create(TokenResponse(newAccessToken))
        } catch (e: JwtException) {
            // Refresh 토큰이 만료된 경우.
            return GlobalResponse.createErrorRes("Refresh token fail. ${e.message}. tr")
        }
    }
}
