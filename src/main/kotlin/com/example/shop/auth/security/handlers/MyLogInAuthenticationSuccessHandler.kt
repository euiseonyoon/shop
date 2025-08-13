package com.example.shop.auth.security.handlers

import com.example.shop.auth.jwt_helpers.MyJwtTokenHelper
import com.example.shop.auth.models.CustomUserDetails
import com.example.shop.auth.models.TokenResponse
import com.example.shop.auth.utils.RefreshTokenStateHelper
import com.example.shop.common.logger.LogSupport
import com.example.shop.common.response.GlobalResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kotlinx.serialization.json.Json
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.authentication.AuthenticationServiceException
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.AuthenticationSuccessHandler

class MyLogInAuthenticationSuccessHandler(
    private val jwtHelper: MyJwtTokenHelper,
    private val json: Json,
    private val refreshTokenStateHelper: RefreshTokenStateHelper
) : AuthenticationSuccessHandler, LogSupport() {
    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val customUserDetail = authentication.principal as? CustomUserDetails
            ?: throw AuthenticationServiceException("Failed to parse to customized user details.")

        val accountId = customUserDetail.account.id!!
        val email = customUserDetail.account.email!!

        val accessToken = jwtHelper.createAccessToken(accountId, authentication.authorities.toList(), email)
        val refreshToken = jwtHelper.createRefreshToken(accountId)

        // 새롭게 발급한 refresh token을 redis에 저장하여 상태관리
        try {
            refreshTokenStateHelper.updateWithNewRefreshToken(accountId, refreshToken)
            // Refresh 토큰을 http only 쿠키에 저장
            jwtHelper.setRefreshTokenOnCookie(response, refreshToken)
        } catch (e: Exception) {
            logger.error("refresh token not saved on redis.")
        }


        // 응답 본문에 토큰을 JSON 형식으로 작성합니다.
        response.status = HttpStatus.OK.value() // HTTP 상태 코드를 200 OK로 설정
        response.contentType = MediaType.APPLICATION_JSON_VALUE // 응답 Content-Type을 JSON으로 설정

        // access token이 포함된 GlobalResponse 작성
        val globalResponse = GlobalResponse.create(TokenResponse(accessToken))
        val jsonResponse = json.encodeToString(GlobalResponse.serializer(TokenResponse.serializer()), globalResponse)

        response.writer.write(jsonResponse)
        response.writer.flush()
    }
}
