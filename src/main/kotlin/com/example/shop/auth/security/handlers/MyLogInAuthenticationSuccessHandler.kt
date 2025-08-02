package com.example.shop.auth.security.handlers

import com.example.shop.auth.jwt_helpers.MyJwtTokenHelper
import com.example.shop.auth.models.TokenResponse
import com.example.shop.auth.utils.RefreshTokenStateHelper
import com.example.shop.common.apis.GlobalResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kotlinx.serialization.json.Json
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.web.authentication.AuthenticationSuccessHandler

class MyLogInAuthenticationSuccessHandler(
    private val jwtHelper: MyJwtTokenHelper,
    private val json: Json,
    private val refreshTokenStateHelper: RefreshTokenStateHelper
) : AuthenticationSuccessHandler {
    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val email = if (authentication.principal is UserDetails) {
            (authentication.principal as UserDetails).username
        } else {
            (authentication.principal as OAuth2User).name
        }

        val accessToken = jwtHelper.createAccessToken(email, authentication.authorities.toList())
        val refreshToken = jwtHelper.createRefreshToken(email)

        // 새롭게 발급한 refresh token을 redis에 저장하여 상태관리
        refreshTokenStateHelper.updateWithNewRefreshToken(email, refreshToken)

        // 응답 본문에 토큰을 JSON 형식으로 작성합니다.
        response.status = HttpStatus.OK.value() // HTTP 상태 코드를 200 OK로 설정
        response.contentType = MediaType.APPLICATION_JSON_VALUE // 응답 Content-Type을 JSON으로 설정

        // Refresh 토큰을 http only 쿠키에 저장
        jwtHelper.setRefreshTokenOnCookie(response, refreshToken)

        // access token이 포함된 GlobalResponse 작성
        val globalResponse = GlobalResponse.create(TokenResponse(accessToken))
        val jsonResponse = json.encodeToString(GlobalResponse.serializer(TokenResponse.serializer()), globalResponse)

        response.writer.write(jsonResponse)
        response.writer.flush()
    }
}
