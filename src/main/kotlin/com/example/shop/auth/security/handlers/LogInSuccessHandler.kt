package com.example.shop.auth.security.handlers

import com.example.shop.auth.domain.AccountDomain
import com.example.shop.auth.jwt_helpers.MyJwtTokenHelper
import com.example.shop.auth.models.TokenResponse
import com.example.shop.auth.utils.RefreshTokenStateHelper
import com.example.shop.common.logger.LogSupport
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component

@Component
class LogInSuccessHandler(
    private val jwtHelper: MyJwtTokenHelper,
    private val refreshTokenStateHelper: RefreshTokenStateHelper,
) : LogSupport() {
    fun getTokenResponse(
        response: HttpServletResponse,
        accountDomain: AccountDomain
    ): TokenResponse {
        val accountId = accountDomain.account.id
        val email = accountDomain.account.email

        val accessToken = jwtHelper.createAccessToken(accountId, accountDomain.authorities, email)
        val refreshToken = jwtHelper.createRefreshToken(accountId)

        // 새롭게 발급한 refresh token을 redis에 저장하여 상태관리
        try {
            refreshTokenStateHelper.updateWithNewRefreshToken(accountId, refreshToken)
            // Refresh 토큰을 http only 쿠키에 저장
            jwtHelper.setRefreshTokenOnCookie(response, refreshToken)
        } catch (e: Exception) {
            logger.error("refresh token not saved on redis.")
        }

        return TokenResponse(accessToken)
    }
}
