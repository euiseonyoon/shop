package com.example.shop.auth.controller

import com.example.shop.auth.TOKEN_REFRESH_URI
import com.example.shop.auth.exceptions.BadRefreshTokenStateException
import com.example.shop.auth.exceptions.RefreshTokenMissingException
import com.example.shop.auth.jwt_helpers.MyJwtTokenHelper
import com.example.shop.auth.models.TokenResponse
import com.example.shop.auth.security.utils.MyJwtTokenExtractor
import com.example.shop.auth.services.AccountService
import com.example.shop.auth.utils.RefreshTokenStateHelper
import com.example.shop.common.apis.GlobalResponse
import com.example.shop.common.logger.LogSupport
import com.example.shop.common.utils.CustomAuthorityUtils
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.security.access.AuthorizationServiceException
import org.springframework.security.oauth2.jwt.JwtException
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class TokenController(
    private val myJwtTokenHelper: MyJwtTokenHelper,
    private val accountService: AccountService,
    private val myJwtTokenExtractor: MyJwtTokenExtractor,
    private val customAuthorityUtils: CustomAuthorityUtils,
    private val refreshTokenStateHelper: RefreshTokenStateHelper,
) : LogSupport() {
    @PostMapping(TOKEN_REFRESH_URI)
    fun refreshTokens(
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): GlobalResponse<TokenResponse> {
        val refreshTokenFromRequest = myJwtTokenExtractor.extractRefreshTokenFromCookie(request)
            ?: throw RefreshTokenMissingException("Can't find refresh token from the cookie.")

        try {
            val claims = myJwtTokenHelper.parseRefreshToken(refreshTokenFromRequest)
            val accountId = myJwtTokenHelper.getSubject(claims)

            // request로 부터 추출한 refresh token과 redis에 저장된 refresh token 비교
            // 비교가 비정상적이라면, BadRefreshTokenStateException 예외 발생
            refreshTokenStateHelper.validateRefreshToken(accountId, refreshTokenFromRequest)

            val account = accountService.findWithAuthoritiesById(accountId)
                ?: throw AuthorizationServiceException("Can't find the account from the database.")

            val newAccessToken = myJwtTokenHelper.createAccessToken(
                accountId,
                customAuthorityUtils.createSimpleGrantedAuthorities(account),
                account.email!!
            )
            val newRefreshToken = myJwtTokenHelper.createRefreshToken(accountId)

            // 새롭게 발급한 refresh token을 redis에 저장하여 상태관리
            refreshTokenStateHelper.updateWithNewRefreshToken(accountId, newRefreshToken)
            myJwtTokenHelper.setRefreshTokenOnCookie(response, newRefreshToken)

            return GlobalResponse.create(TokenResponse(newAccessToken))
        } catch (e: Exception) {
            val errorResult = handleTokenRefreshException(response, e)
            return errorResult
        }
    }

    private fun handleTokenRefreshException(
        response: HttpServletResponse,
        e: Exception
    ): GlobalResponse<TokenResponse> {
        when (e) {
            is RefreshTokenMissingException -> {
                // Refresh token을 request의 쿠키에서 추출하지 못한 경우.
                myJwtTokenHelper.deleteRefreshTokenFromCookie(response)
                return GlobalResponse.createErrorRes(response, e, null)
            }
            is AuthorizationServiceException -> {
                // Refresh 토큰에서 추출한 email로 account를 찾을 수 없는 경우.
                myJwtTokenHelper.deleteRefreshTokenFromCookie(response)
                return GlobalResponse.createErrorRes(response, e.message!!, HttpStatus.UNAUTHORIZED)
            }
            is JwtException -> {
                // Refresh 토큰이 만료된 경우.
                myJwtTokenHelper.deleteRefreshTokenFromCookie(response)
                return GlobalResponse.createErrorRes(
                    response,
                    "Refresh token fail. ${e.message}. try login again.",
                    HttpStatus.BAD_REQUEST,
                )
            }
            is BadRefreshTokenStateException -> {
                // Request로 부터 추출한 refresh token과 redis에 저장된 refresh token을 비교 검증했을때, 비정상적인 경우.
                myJwtTokenHelper.deleteRefreshTokenFromCookie(response)
                return GlobalResponse.createErrorRes(response, e, null)
            }
            else -> throw e
        }
    }
}
